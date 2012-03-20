package actors

import akka.actor.Actor
import org.slf4j.LoggerFactory
import scala.io.Source


class EventScraper extends Actor {
  val log = LoggerFactory.getLogger(this.getClass)
  val PsychSheetPattern = """\s*(\d+|-+)\s+(\w+), ([\w\s]+)\s+(\d+)\s+([\D]*)\s+(NT|NS|SCR|[0-9:.]+)\s*$""".r
  val CompletedResultPattern = """\s*(\d+|-+)\s+(\w+), ([\w\s]+)\s+(\d+)\s+([\D]*)\s+(NT|NS|SCR|[0-9:.]+)\s+(NT|NS|SCR|[0-9:.]+)\s*.*""".r

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
    var completedCount =0;
    var incompleteCount = 0;
    try {
      val page = Source.fromURL(event.url)
      // Parse results
      for (line <- page.getLines()) line match {
        case CompletedResultPattern(place, lastName, firstName, age, team, seed, finals) =>
          completedCount += 1
          // TODO: Strip trailing initial from first name if present (ie: "Fred A")
          val result = new ScrapedResult(event, new Person(firstName.trim, lastName.trim), age.toInt, team.trim, place, seed, finals)
          resultProcessor ! result
        case PsychSheetPattern(place, lastName, firstName, age, team, seed)  =>
          incompleteCount += 1
        case line => log.trace("Unmatched line: {}",line)
      }
    } catch {
      case e: Exception => log.error("Error scraping event {}: {}", event.id, e)
    }
    val eventCompleted = completedCount > 0 & incompleteCount == 0
    log.debug("Done scraping event {}, {} with final times, {} without", Array[AnyRef](event.id, completedCount.toString, incompleteCount.toString))
    sender ! EventScraped(event, eventCompleted)
  }
}
