package actors

import org.slf4j.LoggerFactory
import akka.actor.{ Props, Actor }
import akka.routing.RoundRobinRouter
import org.joda.time.DateTime
import models.Meet
import java.util.Date
import java.text.SimpleDateFormat
import dispatch.classic._
import grizzled.slf4j.Logging

class Driver extends Actor with AdminNotifier with Logging {
  val MAX_WAIT = 60000

  val meetScraper = context.actorOf(Props[MeetScraper].withRouter(RoundRobinRouter(5)), name = "meetScraper")

  def receive = {
    case meetReq: ScrapeMeet => meetScraper forward meetReq
    case ScrapeAllMeets => scrapeAll
  }

  def scrapeAll = {
    info("Processing all registered meet URLs")
    // Check all of the registered meet URLs
    val twoWeeksAgo = DateTime.now.minusWeeks(2).toDate
    var count = 0;
    for (url <- Meet.findAll) {
      val lastMod = getLastModified(url)
      debug("Last modified date for URL " + url.id + " is " + lastMod)
      if (url.inProgress) {
        // Mark as complete if we've been scraping for 2 weeks without completion
        if (lastMod.compareTo(twoWeeksAgo) < 0) {
          warn("Meet" + url.id + " has been in progress for two weeks, marking as complete")
          sendAdminEmail("Meet expiration notice", "Meet has not completed for two weeks, marking complete: %s".format(url.id))
          Meet.save(url.copy(inProgress = false, lastCompleted = Some(new Date)))
        } else {
          // Otherwise, scrape the meet for latest results
          debug("Scraping in-progress meet: " + url.id)
          self ! ScrapeMeet(url)
          count += 1
        }
      } else if (lastMod.compareTo(url.lastCompleted getOrElse new Date(0)) > 0) {
        // Completed date is older than the last modified date, this URL is active again
        info("Meet " + url.id + " has become active")
        Meet.save(url.copy(inProgress = true))
        sendAdminEmail("Meet activation notice", "Meet URL has become active: %s".format(url.id))
        self ! ScrapeMeet(url)
        count += 1
      }
    }
    if (count == 0) info("No active meets")
  }

  def getLastModified(meetUrl: Meet): Date = {
    val dateParser = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z")
    val idxUrl = meetUrl.id + "/evtindex.htm"
    val u = url(idxUrl)
    debug("Executing HEAD request for " + idxUrl)
    Http(u.HEAD >:> {
      headers: Map[String, Set[String]] =>
        {
          headers.get("Last-Modified") match {
            case Some(vals) => dateParser.parse(vals.head)
            case _ =>
              warn("URL " + idxUrl + " has no Last-Modified header, using current date")
              new Date()
          }
        }
    })
  }

}
