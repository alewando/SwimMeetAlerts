package actors

import org.slf4j.LoggerFactory
import akka.actor.{Props, Actor}
import akka.routing.RoundRobinRouter
import org.joda.time.DateTime
import model.MeetUrl
import java.util.Date
import java.text.SimpleDateFormat
import dispatch._

class Driver extends Actor {
  val log = LoggerFactory.getLogger(this.getClass)
  val MAX_WAIT = 60000

  val meetScraper = context.actorOf(Props[MeetScraper].withRouter(RoundRobinRouter(5)), name = "meetScraper")

  def receive = {
    case meetReq: ScrapeMeet => meetScraper forward meetReq
    case ScrapeAllMeets => scrapeAll
  }

  def scrapeAll = {
    log.info("Processing all registered meet URLs")
    // Check all of the registered meet URLs
    val twoWeeksAgo = DateTime.now.minusWeeks(2).toDate
    var count = 0;
    for (url <- MeetUrl.findAll) {
      val lastMod = getLastModified(url)
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
          count += 1
        }
      } else if (lastMod.compareTo(url.lastCompleted.is) > 0) {
        // Completed date is older than the last modified date, this URL is active again
        log.info("Meet {} has become active", url.id.is)
        url.inProgress(true).save
        log.debug("Scraping newly active meet: {}", url.id.is)
        self ! ScrapeMeet(url)
        count += 1
      }
    }
    if (count == 0) log.info("No active meets")
  }

  def getLastModified(meetUrl: MeetUrl): Date = {
    val dateParser = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z")
    val idxUrl = meetUrl.id.is + "/evtindex.htm"
    val u = url(idxUrl)
    log.debug("Executing HEAD request for {}", idxUrl)
    Http(u.HEAD >:> {
      headers: Map[String, Set[String]] => {
        headers.get("Last-Modified") match {
          case Some(vals) => dateParser.parse(vals.head)
          case _ =>
            log.warn("URL {} has no Last-Modified header, using current date", idxUrl)
            new Date()
        }
      }
    })
  }


}
