package actors

import akka.actor.Actor
import org.slf4j.LoggerFactory
import scala.io.Source


class EventScraper extends Actor {
  val log = LoggerFactory.getLogger(this.getClass)
  val EntrantPattern = """\s*(\d+|-+)\s+(\w+), ([\w\s]+)\s+(\d+)\s+([\D]*)\s+(NT|NS|SCR|[0-9:.]+)\s+(NT|NS|SCR|[0-9:.]+)\s*.*""".r

  val resultProcessor = context.actorFor("/user/resultProcessor")

  def receive = {
    case event: Event =>
      scrapeEvent(event)
  }

  /**
   * Scrape results from a specific event
   */
  def scrapeEvent(event: Event) {
    log.debug("Scraping event {} for meet {}", event.id, event.meet.name)
    try {
      val page = Source.fromURL(event.url)
      // Parse results
      for (line <- page.getLines(); m <- EntrantPattern findAllIn line) m match {
        case EntrantPattern(place, lastName, firstName, age, team, seed, finals) =>
          // TODO: Strip trailing initial from first name if present (ie: "Fred A")
          resultProcessor ! new ScrapedResult(event, new Person(firstName.trim, lastName.trim), age.toInt, team.trim, place, seed, finals)
        // TODO: Save event as completed if all entrants have final results
        case _ =>
          log.trace("Line: {}", line)
      }
    } catch {
      case e: Exception => log.error("Error scraping event {}: {}", event.id, e)
    }
    log.trace("Done scraping event {}", event.id)
  }
}
