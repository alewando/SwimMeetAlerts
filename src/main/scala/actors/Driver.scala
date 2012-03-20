package actors

import org.slf4j.LoggerFactory
import akka.actor.{Props, Actor}
import akka.routing.RoundRobinRouter

class Driver extends Actor {
  val log = LoggerFactory.getLogger(this.getClass)
  val MAX_WAIT = 60000

  val EventLink = """^<a href="(.+).htm" target=main>([^<]*)</a>.*""".r;

  val eventScraper = context.actorOf(Props[EventScraper].withRouter(RoundRobinRouter(15)), name = "eventScraper")

  def receive = {
    case meet: Meet => scrapeMeet(meet)
  }

  // TODO: Create a MeetScraper actor to do this
  def scrapeMeet(meet: Meet) = {
    // Scrape events from meet page
    for (event <- events(meet)) {
      // Send each event as a message to the actors
      log.trace("Sending message for event: {}", event.id)
      eventScraper ! event
    }
    log.debug("Done scraping event list for meet: {}", meet.name)
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
