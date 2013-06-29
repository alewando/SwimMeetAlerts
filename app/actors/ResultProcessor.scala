package actors

import org.slf4j.LoggerFactory
import models.{ User, Swimmer }
import akka.actor.Actor
import grizzled.slf4j.Logging

/**
 * Processes a scraped record. Saves new results to DB and sends an email.
 */
class ResultProcessor extends Actor with Logging {

  val emailSender = context.actorFor("/user/emailSender")

  def receive = {
    case result: ScrapedResult =>
      handleResult(result)
  }

  def handleResult(scrapedResult: ScrapedResult) {

    trace(scrapedResult);

    // See if this scrapedResult is for a swimmer being watched
    val swimmer = Swimmer.findForResult(scrapedResult) getOrElse {
      //log.debug("Not a tracked swimmer: {}", scrapedResult.entrant)
      return
    }
    debug("Result for tracked swimmer: " + scrapedResult)

    // Look for existing scrapedResult in DB
    if (!swimmer.resultExists_?(scrapedResult)) {
      // Create and save new scrapedResult record
      info("Saving new scrapedResult for tracked swimmer: " + scrapedResult)
      val result = scrapedResult.mapToRecord()
      swimmer.addResult(result)

      // Get emails for swimmer's watchers
      val emailRecips: List[String] = getEmailRecipientsForSwimmer(swimmer) match {
        case Nil => {
          info("Swimmer " + swimmer.fullName + " is being tracked, but has no follower email addresses")
          return
        }
        case x => x
      }
      emailSender ! (swimmer, result, emailRecips)
    }
  }

  def getEmailRecipientsForSwimmer(swimmer: Swimmer): List[String] = {
    val recipLists = for (
      watcher <- swimmer.watchers;
      u <- User.findOneById(watcher)
    ) yield {
      u.email :: u.extraDestinations
    }
    recipLists.flatten
  }
}

