import com.sun.tools.javac.util.Log
import org.slf4j.LoggerFactory
import scala.io.Source

case class Meet(baseUrl: String, teamId: String) {
  def url: String = {
    baseUrl + "/" + teamId;
  }

  def eventsPage = {
    val eventsUrl = url + "/evtindex.htm"
    Source.fromURL(eventsUrl)
  }

  var meetName: String = null;

  def name: String = {
    if (meetName == null) {
      val MeetNamePattern = """<h2.*>(.*)</h2>""".r
      for (line <- eventsPage.getLines; m <- MeetNamePattern findFirstIn line) m match {
        case MeetNamePattern(name) => meetName = name.trim();
      }
    }
    meetName
  }
}

case class Event(id: String, name: String)

case class Person(firstName: String, lastName: String) {
  def fullName: String = {
    firstName + " " + lastName;
  }
}

case class Result(entrant: Person, age: Int, team: String, place: String, seedTime: String, finalTime: String)

class Scraper(meet: Meet) {
  val log = LoggerFactory.getLogger(this.getClass())
  val EventLink = """^<a href="(.+).htm" target=main>([^<]*)</a>.*""".r;
  val EntrantPattern = """\s*(\d+|-+)\s*(\w+), (\w+)\s*(\d+)\s*([\D]*)\s*([0-9:.NST]+)\s+([0-9:.NST]+)\s*.*""".r

  def events: List[Event] = {

    var lEvents = List[Event]()
    for (line <- meet.eventsPage.getLines; m <- EventLink findAllIn line) m match {
      case EventLink(id, name) => lEvents = new Event(id, name.trim) :: lEvents
    }

    lEvents.reverse
  }

  //TODO: Return Iterator[Result]
  def eventResults(eventId: String): List[Result] = {
    val eventUrl = meet.url + "/" + eventId + ".htm";
    try {
      val page = Source.fromURL(eventUrl)
      // Parse results
      var lResults = List[Result]();
      for (line <- page.getLines(); m <- EntrantPattern findAllIn line) m match {
        case EntrantPattern(place, lastName, firstName, age, team, seed, finals) => lResults = new Result(new Person(firstName, lastName), age.toInt, team.trim, place, seed, finals) :: lResults;
      }
      lResults.reverse
    } catch {
      case e: Exception => log.error("Error scraping event " + eventId + ": " + e)
      List()
    }
  }
}
