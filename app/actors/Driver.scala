package actors

import org.slf4j.LoggerFactory
import akka.actor.{ Props, Actor }
import akka.routing.RoundRobinRouter
import org.joda.time.DateTime
import models.Meet
import java.util.Date
import java.text.SimpleDateFormat
import dispatch.classic._
import com.typesafe.scalalogging.slf4j.Logging
import com.mongodb.casbah.WriteConcern

class Driver extends Actor with AdminNotifier with Logging {
  val MAX_WAIT = 60000

  val meetScraper = context.actorOf(Props[MeetScraper].withRouter(RoundRobinRouter(5)), name = "meetScraper")

  def receive = {
    case meetReq: ScrapeMeet => meetScraper forward meetReq
    case ScrapeAllMeets => scrapeAll
  }

  def scrapeAll = {
    logger.info("Processing all registered meet URLs")
    // Check all of the registered meet URLs
    val twoWeeksAgo = DateTime.now.minusWeeks(2).toDate
    var count = 0;
    for (url <- Meet.findAll) {
      val lastMod = getLastModified(url)
      logger.debug(s"Last modified date for URL ${url.id} is ${lastMod}. Last completed: ${url.lastCompleted}")
      if (url.inProgress) {
        // Mark as complete if we've been scraping for 2 weeks without completion
        if (lastMod.compareTo(twoWeeksAgo) < 0) {
          logger.warn(s"Meet ${url.id} has been in progress for two weeks, marking as complete")
          sendAdminEmail("Meet expiration notice", "Meet has not completed for two weeks, marking complete: %s".format(url.id))
          Meet.save(url.copy(inProgress = false, lastCompleted = Some(new Date)), WriteConcern.Safe)
        } else {
          // Otherwise, scrape the meet for latest results
          logger.debug(s"Scraping in-progress meet: ${url.id}")
          self ! ScrapeMeet(url)
          count += 1
        }
      } else if (lastMod.compareTo(url.lastCompleted getOrElse new Date(1)) > 0) {
        // Completed date is older than the last modified date, this URL is active again
        logger.info(s"Meet ${url.id} has become active")
        val inProgressMeet = url.copy(inProgress = true)
        Meet.save(inProgressMeet)
        sendAdminEmail("Meet activation notice", "Meet URL has become active: %s".format(url.id))
        self ! ScrapeMeet(inProgressMeet)
        count += 1
      }
    }
    if (count == 0) logger.info("No active meets")
  }

  def getLastModified(meetUrl: Meet): Date = {
    val dateParser = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z")
    val idxUrl = meetUrl.id + "/evtindex.htm"
    val u = url(idxUrl)
    logger.debug(s"Executing HEAD request for ${idxUrl}")
    try {
      Http(u.HEAD >:> {
        headers: Map[String, Set[String]] =>
          {
            headers.get("Last-Modified") match {
              case Some(vals) => dateParser.parse(vals.head)
              case _ =>
                logger.warn(s"URL ${idxUrl} has no Last-Modified header, using current date")
                new Date()
            }
          }
      })
    } catch {
      case ex: Throwable => {
        logger.error(s"Error retrieving headers for ${meetUrl}", ex)
        new Date()
      }
    }
  }

}
