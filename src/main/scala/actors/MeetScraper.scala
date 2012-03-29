package actors

import org.slf4j.LoggerFactory
import akka.routing.RoundRobinRouter
import akka.actor.{Props, Actor}
import akka.pattern._
import akka.util.duration._
import akka.dispatch.Future
import java.util.Date
import io.Source
import model.CompletedEvent

class MeetScraper extends Actor {

  val log = LoggerFactory.getLogger(this.getClass)

  val eventScraper = context.actorOf(Props[EventScraper].withRouter(RoundRobinRouter(15)), name = "eventScraper")
  val MeetNamePattern = """^<h2.*>(.*)</h2>.*""".r
  val EventLink = """^<a href="(.+).htm" target=main>([^<]*)</a>.*""".r;

  def receive = {
    case meet: ScrapeMeet => scrapeMeet(meet)
    case EventScraped(event, completed) => log.info("Event {} scraped. Completed: {}", event.name, completed)
  }

  def scrapeMeet(meet: ScrapeMeet) = {
    // Scrape events from meet page
    val (meetName, events) = getEventInfo(meet)
    // Update meet name in DB
    meet.url.name(meetName).save
    // Scrape each event (async)
    val eventScrapes = for (event <- events; if !CompletedEvent.eventCompleted_?(meetName, event.url)) yield {
      eventScraper.ask(event)(20 seconds).mapTo[EventScraped]
    }
    // Get overall meet status (completed) by folding up status of individual event futures. Any non-completed event
    // will cause the entire meet to be considered incomplete
    import context.dispatcher
    val meetCompleted = Future.fold(eventScrapes)(true)((agg: Boolean, evt: EventScraped) => agg && evt.completed)
    //val meetCompleted = Await.result(meetCompletedFuture, 5 minutes);
    for (x <- meetCompleted; if x) {
      // Save meet status
      meet.url.inProgress(false).lastCompleted(new Date()).save
      log.info("Meet \"{}\" is completed ({})", meetName, meet.url.id.is)
      // Remove records for this meet from CompletedEvents collection
      CompletedEvent.deleteEventsForMeet(meetName)
    }
  }

  /**
   * Scrape events for a specific meet
   */
  def getEventInfo(meet: ScrapeMeet): (String, List[Event]) = {
    var lEvents: List[Event] = Nil
    var meetName: String = ""
    // TODO: Use Dispatch client to get event list
    for (line <- Source.fromURL(meet.url.eventIndexUrl).getLines()) line match {
      case MeetNamePattern(name) =>
        log.debug("Meet Name for url {} is \"{}\"", meet.url, name)
        meetName = name.trim()
      case EventLink(id, name) =>
        val eventUrl = meet.url.id.is + "/" + id + ".htm"
        lEvents = new Event(id, meetName, name.trim, eventUrl) :: lEvents
      case _ => log.trace("Ignoring line: {}", line)
    }
    (meetName, lEvents.reverse)
  }

}
