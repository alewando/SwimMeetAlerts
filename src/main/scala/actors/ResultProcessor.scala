package actors

import org.slf4j.LoggerFactory
import net.liftweb.mongodb.BsonDSL._
import model.{User, Result, Swimmer}
import akka.actor.Actor

/**
 * Processes a scraped record. Saves new results to DB and sends an email.
 */
class ResultProcessor extends Actor {
  val log = LoggerFactory.getLogger(this.getClass)

  val emailSender = context.actorFor("../emailSender")

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
    val existing = Result.find(("swimmer" -> swimmer.id.is) ~ ("event" -> scrapedResult.event.name) ~ ("meet" -> scrapedResult.event.meet.name))
    existing openOr {
      // Create and save new scrapedResult record
      log.info("Saving new scrapedResult for tracked swimmer: {}", scrapedResult)
      val result = model.Result.createRecord.meet(scrapedResult.event.meet.name).event(scrapedResult.event.name).age(scrapedResult.age).team(scrapedResult.team).seedTime(scrapedResult.seedTime).finalTime(scrapedResult.finalTime)
      swimmer.addResult(result)

      // Get emails for swimmer's watchers
      val emailRecips = getEmailRecipientsForSwimmer(swimmer) match {
        case Nil => return
        case x: List[String] => x
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


