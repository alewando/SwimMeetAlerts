import actors.Actor
import scala.actors.Actor._
import org.slf4j.LoggerFactory
import scala.io.Source


object Scraper extends Actor {
  val log = LoggerFactory.getLogger(this.getClass)
  val EventLink = """^<a href="(.+).htm" target=main>([^<]*)</a>.*""".r;
  val EntrantPattern = """\s*(\d+|-+)\s+(\w+), (\w+)\s+(\d+)\s+([\D]*)\s+(NT|NS|SCR|[0-9:.]+)\s+(NT|NS|SCR|[0-9:.]+)\s*.*""".r

  def scrapeMeet(meet: Meet) = {
    // Scrape events from meet page
    for (event <- events(meet)) {
      // Send each event as a message to the scraper
      log.debug("Sending message for event: " + event.id)
      this ! event
    }
    log.info("Done scraping meet: " + meet.name)
    this ! Stop
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

  def act() {
    log.debug("Scraper.act")
    loop {
      react {
        case event: Event => actor {
          try {
            Coordinator.taskStarted
            log.debug("Scraper.react.event")
            scrapeEvent(event)
          } finally {
            Coordinator.taskFinished
          }
        }
        case Stop => {
          log.debug("Scraper.react.stop")
          ResultProcessor ! Stop
          exit()
        }
      }
    }
  }

  /**
   * Scrape results from a specific event
   */
  def scrapeEvent(event: Event) {
    log.debug("Scraping event " + event.id)
    try {
      val page = Source.fromURL(event.url)
      // Parse results
      for (line <- page.getLines(); m <- EntrantPattern findAllIn line) m match {
        case EntrantPattern(place, lastName, firstName, age, team, seed, finals) =>
          // Publish meesage to result processor
          ResultProcessor ! new Result(event, new Person(firstName, lastName), age.toInt, team.trim, place, seed, finals)
        // TODO: Save event as completed if all entrants have final results
        case _ => if(log.isTraceEnabled()) { log.trace("Line: "+line) }
      }
    } catch {
      case e: Exception => log.error("Error scraping event " + event.id + ": " + e)
    }
    log.debug("Done scraping event " + event.id)
  }
}
