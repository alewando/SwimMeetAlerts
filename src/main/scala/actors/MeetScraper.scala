package actors

import org.slf4j.LoggerFactory
import akka.routing.RoundRobinRouter
import akka.actor.{Props, Actor}
import akka.pattern._
import akka.util.duration._
import akka.dispatch.{Await, Future}

class MeetScraper extends Actor {

  val log = LoggerFactory.getLogger(this.getClass)

  val eventScraper = context.actorOf(Props[EventScraper].withRouter(RoundRobinRouter(15)), name = "eventScraper")
  val EventLink = """^<a href="(.+).htm" target=main>([^<]*)</a>.*""".r;

  def receive = {
    case meet: Meet => scrapeMeet(meet)
    case EventScraped(event, completed) => log.info("Event {} scraped. Completed: {}", event.name, completed)
  }

  def scrapeMeet(meet: Meet) = {
    // Scrape events from meet page
    val eventScrapes = for (event <- events(meet)) yield {
      // Send each event as a message to the actors
      log.trace("Sending message for event: {}", event.id)
      eventScraper.ask(event)(20 seconds).mapTo[EventScraped]
    }
    // Get overall meet status (completed) by folding up status of individual events. Any non-completed event
    // will cause the entire meet to be considered incomplete
    import context.dispatcher
    val meetCompletedFuture = Future.fold(eventScrapes)(true)((agg: Boolean, evt: EventScraped) => agg && evt.completed)
    val meetCompleted = Await.result(meetCompletedFuture, 5 minutes);
    log.debug("Meet {} completed: {}", meet.name, meetCompleted)
  }

  /**
   * Scrape events for a specific meet
   */
  def events(meet: Meet): List[Event] = {
    var lEvents = List[Event]()
    for (line <- meet.eventsPage.getLines(); m <- EventLink findAllIn line) m match {
      case EventLink(id, name) =>
        val eventUrl = meet.url + "/" + id + ".htm"
        lEvents = new Event(id, meet, name.trim, eventUrl) :: lEvents
    }
    lEvents.reverse
  }

}
