package actors

import org.slf4j.LoggerFactory
import akka.actor.{Props, Actor}
import akka.routing.RoundRobinRouter
import org.joda.time.DateTime
import model.MeetUrl
import java.util.Date
import cc.spray.http.HttpRequest
import cc.spray.client.HttpConduit
import cc.spray.http.HttpMethods._
import java.text.SimpleDateFormat
import akka.dispatch.Future
import java.net.URL

class Driver extends Actor {
  val log = LoggerFactory.getLogger(this.getClass)
  val MAX_WAIT = 60000

  val meetScraper = context.actorOf(Props[MeetScraper].withRouter(RoundRobinRouter(5)), name = "meetScraper")

  def receive = {
    case meetReq: ScrapeMeet => meetScraper forward meetReq
    //case ScrapeAllMeets => scrapeAll
  }

  def scrapeAll = {
    log.info("Processing all registered meet URLs")
    // Check all of the registered meet URLs
    val twoWeeksAgo = DateTime.now.minusWeeks(2).toDate
    for (url <- MeetUrl.findAll) {
      log.debug("Meet URL is {}", url.id.is)
    }
    for (url <- MeetUrl.findAll; lastMod <- getLastModified(url)) {
      log.debug("Last modified date for URL {} is {}", url.id.is, lastMod)
      if (url.inProgress.value) {
        // Mark as complete if we've been scraping for 2 weeks without completion
        if (lastMod.compareTo(twoWeeksAgo) < 0) {
          log.warn("Meet {} has been in progress for two weeks, marking as complete", url.id.is)
          url.inProgress(false).lastCompleted(new Date()).save
        } else {
          // Otherwise, scrape the meet for latest results
          log.debug("Scraping in-progress meet: {}", url.id.is)
          self ! ScrapeMeet(url)
        }
      } else if (lastMod.compareTo(url.lastCompleted.is) > 0) {
        // Completed date is older than the last modified date, this URL is active again
        log.info("Meet {} has become active", url.id.is)
        url.inProgress(true).save
        log.debug("Scraping newly active meet: {}", url.id.is)
        self ! ScrapeMeet(url)
      }
    }
  }

  def getLastModified(url: MeetUrl): Future[Date] = {
    val dateParser = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z")
    val u = new URL(url.id.is)
    log.debug("creating conduit")
    val conduit = new HttpConduit(u.getHost())
    log.debug("Executing HEAD request for {}", u)
    val headResp = conduit.sendReceive(HttpRequest(HEAD, "/evtindex.htm"))
    headResp map {
      resp => resp.headers.find(_.name equals "Last-Modified") map {
        x =>
          dateParser.parse(x.value)
      } getOrElse {
        log.warn("URL {} has no Last-Modified header, using current date")
        new Date()
      }
    }
  }


}
