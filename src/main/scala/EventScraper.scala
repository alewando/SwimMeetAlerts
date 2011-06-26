import actors.Actor
import scala.actors.Actor._
import org.slf4j.LoggerFactory
import scala.io.Source


class Scraper(meet: Meet) extends Actor {
  val log = LoggerFactory.getLogger(this.getClass)
  val EventLink = """^<a href="(.+).htm" target=main>([^<]*)</a>.*""".r;
  val EntrantPattern = """\s*(\d+|-+)\s*(\w+), (\w+)\s*(\d+)\s*([\D]*)\s*([0-9:.NST]+)\s+([0-9:.NST]+)\s*.*""".r

  def events: List[Event] = {

    var lEvents = List[Event]()
    for (line <- meet.eventsPage.getLines(); m <- EventLink findAllIn line) m match {
      case EventLink(id, name) => lEvents = new Event(id, name.trim) :: lEvents
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
          ResultProcessor ! Stop
          exit()
        }
      }
    }
  }

  def scrapeEvent(event: Event) {
    log.debug("Scraping event " + event.id)
    val eventUrl = meet.url + "/" + event.id + ".htm";
    try {
      val page = Source.fromURL(eventUrl)
      // Parse results
      for (line <- page.getLines(); m <- EntrantPattern findAllIn line) m match {
        case EntrantPattern(place, lastName, firstName, age, team, seed, finals) =>
          // Publish meesage to result processor
          ResultProcessor ! new Result(meet, event, new Person(firstName, lastName), age.toInt, team.trim, place, seed, finals)
      }
    } catch {
      case e: Exception => log.error("Error scraping event " + event.id + ": " + e)
    }
    log.debug("Done scraping event " + event.id)
  }
}
