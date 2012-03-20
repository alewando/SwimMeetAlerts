package actors

import org.slf4j.LoggerFactory
import model.{User, Swimmer}
import akka.actor.Actor

/**
 * Processes a scraped record. Saves new results to DB and sends an email.
 */
class ResultProcessor extends Actor {
  val log = LoggerFactory.getLogger(getClass)

  val emailSender = context.actorFor("/user/emailSender")

  def receive = {
    case result: ScrapedResult =>
      handleResult(result)
  }

  def handleResult(scrapedResult: ScrapedResult) {
    log.trace("{}", scrapedResult);

    // See if this scrapedResult is for a swimmer being watched
    val swimmer = Swimmer.findForResult(scrapedResult) openOr {
      return
    }
    log.debug("Result for tracked swimmer: " + scrapedResult)

    // Look for existing scrapedResult in DB
    if (!swimmer.resultExists_?(scrapedResult)) {
      // Create and save new scrapedResult record
      log.info("Saving new scrapedResult for tracked swimmer: {}", scrapedResult)
      val result = scrapedResult.mapToRecord()
      swimmer.addResult(result)

      // Get emails for swimmer's watchers
      val emailRecips : List[String]= getEmailRecipientsForSwimmer(swimmer) match {
        case Nil => {
          log.info("Swimmer {} is being tracked, but has no follower email addresses", swimmer.name.value.fullName)
          return
        }
        case x => x
      }
      emailSender !(swimmer, result, emailRecips)
    }
  }

  def getEmailRecipientsForSwimmer(swimmer: Swimmer): List[String] = {
    for (watcher <- swimmer.watchers.value;
         u <- User.find(watcher)
    ) yield {
      u.email.value
    }
  }
}


