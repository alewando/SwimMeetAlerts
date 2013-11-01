package actors

import org.slf4j.LoggerFactory
import akka.routing.RoundRobinRouter
import akka.actor.{ Props, Actor }
import akka.pattern._
import akka.util._
import scala.concurrent.duration._
import scala.concurrent.Future
import java.util.Date
import io.Source
import models._
import com.typesafe.scalalogging.slf4j.Logging

class MeetScraper extends Actor with AdminNotifier with Logging {

  val eventScraper = context.actorOf(Props[EventScraper].withRouter(RoundRobinRouter(15)), name = "eventScraper")
  val MeetNamePattern = """^<h2.*>(.*)</h2>.*""".r
  val EventLink = """^<a href="(.+).htm" target=main>([^<]*)</a>.*""".r;

  def receive = {
    case meet: ScrapeMeet => scrapeMeet(meet)
    case EventScraped(event, completed) => logger.info(s"Event {event.name} scraped. Completed: ${completed}")
  }

  def scrapeMeet(meet: ScrapeMeet) = {
    // Scrape events from meet page
    val (meetName, events) = getEventInfo(meet)
    // Update meet name in DB    
    Meet.save(meet.url.copy(name = meetName))
    // Scrape each event (async)
    val eventScrapes = for (event <- events; if !CompletedEvent.eventCompleted_?(meetName, event.url)) yield {
      implicit val timeout = Timeout(20 seconds)
      eventScraper.ask(event).mapTo[EventScraped]
    }
    // Get overall meet status (completed) by folding up status of individual event futures. Any non-completed event
    // will cause the entire meet to be considered incomplete
    import context.dispatcher
    val meetCompleted = Future.fold(eventScrapes)(true)((agg: Boolean, evt: EventScraped) => agg && evt.completed)
    //val meetCompleted = Await.result(meetCompletedFuture, 5 minutes);
    for (x <- meetCompleted; if x) {
      // TODO: Move meet completion logic to separate method
      sendAdminEmail("Meet completion notice", "Meet completed (all events complete) : %s".format(meet.url.id))
      // Save meet status
      Meet.save(meet.url.copy(inProgress = false, lastCompleted = Some(new Date)))
      logger.info(s"Meet '${meetName}' is completed (${meet.url.id})")
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
        logger.debug(s"Meet Name for url ${meet.url} is '${name}'")
        meetName = name.trim()
      case EventLink(id, name) =>
        val eventUrl = meet.url.id + "/" + id + ".htm"
        val event = new Event(id, meetName, name.trim, eventUrl)
        //logger.debug(s"Found event ${id}: ${name.trim}")
        lEvents = event :: lEvents
      case _ => logger.trace(s"Ignoring line: ${line}")
    }
    (meetName, lEvents.reverse)
  }

}
