package actors

import org.slf4j.LoggerFactory
import akka.routing.RoundRobinRouter
import akka.actor.{Props, Actor}
import akka.pattern._
import akka.util.duration._
import akka.dispatch.Future
import java.util.Date

class MeetScraper extends Actor {

  val log = LoggerFactory.getLogger(this.getClass)

  val eventScraper = context.actorOf(Props[EventScraper].withRouter(RoundRobinRouter(15)), name = "eventScraper")
  val EventLink = """^<a href="(.+).htm" target=main>([^<]*)</a>.*""".r;

  def receive = {
    case meet: ScrapeMeet => scrapeMeet(meet)
    case EventScraped(event, completed) => log.info("Event {} scraped. Completed: {}", event.name, completed)
  }

  def scrapeMeet(meet: ScrapeMeet) = {
    //TODO: Also scrape meet name
    // Scrape events from meet page
    val eventScrapes = for (event <- events(meet)) yield {
      // Send each event as a message to the actors
      log.trace("Sending message for event: {}", event.id)
      eventScraper.ask(event)(20 seconds).mapTo[EventScraped]
    }
    // Get overall meet status (completed) by folding up status of individual events. Any non-completed event
    // will cause the entire meet to be considered incomplete
    import context.dispatcher
    val meetCompleted = Future.fold(eventScrapes)(true)((agg: Boolean, evt: EventScraped) => agg && evt.completed)
    //val meetCompleted = Await.result(meetCompletedFuture, 5 minutes);
    for(x <- meetCompleted; if x) {
      // Save meet status
      meet.url.inProgress(false).lastCompleted(new Date()).save
      log.info("Meet {} is completed", meet.name)
    }    
  }

  /**
   * Scrape events for a specific meet
   */
  def events(meet: ScrapeMeet): List[Event] = {
    var lEvents = List[Event]()
    for (line <- meet.eventsPage.getLines(); m <- EventLink findAllIn line) m match {
      case EventLink(id, name) =>
        val eventUrl = meet.url.id.is + "/" + id + ".htm"
        lEvents = new Event(id, meet.name, name.trim, eventUrl) :: lEvents
    }
    lEvents.reverse
  }

}
