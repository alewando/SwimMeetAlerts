import scala.io.Source;

case class Meet(baseUrl: String, teamId: String) {
  def eventUrl: String = {
    baseUrl + "/" + teamId;
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
  val EventLink = """^<a href="(.+).htm" target=main>([^<]*)</a>.*""".r;
  val EntrantPattern = """\s*(\d+|-+)\s*(\w+), (\w+)\s*(\d+)\s*([\D]*)\s*([0-9:.NST]+)\s+([0-9:.NST]+)\s*.*""".r

  def events: List[Event] = {
    val meetUrl = meet.eventUrl + "/evtindex.htm";
    val eventPage = Source.fromURL(meetUrl);

    var lEvents = List[Event]()
    for (line <- eventPage.getLines; m <- EventLink findAllIn line) m match {
      case EventLink(id, name) => lEvents = new Event(id, name.trim) :: lEvents
    }

    lEvents.reverse
  }

  //TODO: Return Iterator[Result]
  def eventResults(eventId:String): List[Result] = {
    val eventUrl = meet.eventUrl + "/" + eventId + ".htm";
    val page = Source.fromURL(eventUrl)
    // Parse results
    var lResults = List[Result]();
    for (line <- page.getLines(); m <- EntrantPattern findAllIn line) m match {
      case EntrantPattern(place, lastName, firstName, age, team, seed, finals) => lResults = new Result(new Person(firstName, lastName), age.toInt, team.trim, place, seed, finals) :: lResults;
    }
    lResults.reverse
  }
}
