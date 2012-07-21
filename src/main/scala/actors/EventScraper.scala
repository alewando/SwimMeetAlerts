package actors

import akka.actor.Actor
import org.slf4j.LoggerFactory
import scala.io.Source
import model.CompletedEvent
import dispatch.jsoup.JSoupHttp._
import dispatch.{Http, url}


class EventScraper extends Actor {
  val log = LoggerFactory.getLogger(this.getClass)
  val ResultWithoutFinalTime = """\s*(\d+|-+)\s+(\w+), ([\w\s]+)\s+(\d+)\s+([\D]*)\s+(NT|NS|SCR|[0-9:.]+)\s*$""".r
  val ResultWithFinalTime = """\s*(\d+|-+)\s+(\w+), ([\w\s]+)\s+(\d+)\s+([\D]*)\s+(NT|NS|SCR|[0-9:.]+)\s+(NT|NS|SCR|[0-9:.]+)\s*.*""".r
  val RelayResultWithFinalTime = """\s*(\d+|-+)\s+(\D+)\s+(NT|NS|SCR|[0-9:.]+)\s+(NT|NS|SCR|[0-9:.]+)\s*.*""".r

  val resultProcessor = context.actorFor("/user/resultProcessor")

  def receive = {
    case event: Event =>
      scrapeEvent(event)
  }

  /**
   * Scrape results from a specific event
   */
  def scrapeEvent(event: Event) {
    log.debug("Scraping event {} for meet {}", event.id, event.meetName)
    var completedCount = 0;
    var incompleteCount = 0;
    try {

      // Retrieve page using Dispatch library, extract body using JSoup
      val request = url(event.url)
      val body = Http(request </> {
        doc => doc.body().text()
      })
      val pageSource = Source.fromString(body)

      // Parse results
      for (line <- pageSource.getLines()) line match {
        case ResultWithFinalTime(place, lastName, firstName, age, team, seed, finals) =>
          completedCount += 1
          val result = new ScrapedResult(event, new Person(firstName.trim, lastName.trim), age.toInt, team.trim, place, seed, finals)
          resultProcessor ! result
        case RelayResultWithFinalTime(place, team, seed, finals) =>
          // We don't do anything with relay results but need to find the completed
          // results so that the event can be marked as complete
          completedCount += 1
        case ResultWithoutFinalTime(place, lastName, firstName, age, team, seed) =>
          // Note this pattern also matches any 'Preliminary' results included on the Finals event page
          incompleteCount += 1
        case line => log.trace("Unmatched line: {}", line)
      }
    } catch {
      case e: Exception => log.error("Error scraping event {}: {}", event.id, e)
    }
    val eventCompleted = completedCount > 0
    if (eventCompleted) CompletedEvent.markEventComplete(event.meetName, event.url)
    log.debug("Done scraping event {}, {} with final times, {} without", Array[AnyRef](event.id, completedCount.toString, incompleteCount.toString))
    sender ! EventScraped(event, eventCompleted)
  }
}
