import io.Source

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
      for (line <- eventsPage.getLines(); m <- MeetNamePattern findFirstIn line) m match {
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

case class Result(meet: Meet, event: Event, entrant: Person, age: Int, team: String, place: String, seedTime: String, finalTime: String)
