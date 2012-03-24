package actors

import io.{BufferedSource, Source}
import model.{MeetUrl, Result}

case class ScrapeMeet(url: MeetUrl) {

  def eventsPage: BufferedSource = {
    Source.fromURL(url.eventIndexUrl)
  }

  // TODO: Move logic for determining name into MeetScraper (when getting event list)
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

case class Event(id: String, meetName: String, name: String, url: String)

case class Person(firstName: String, lastName: String) {
  def fullName: String = {
    firstName + " " + lastName;
  }
}

/**
 * Passed to indicate an event page has been scraped. The completed flag indicates
 * that the event is completed (entrants have final times)
 */
case class EventScraped(event: Event, completed: Boolean)

case class ScrapedResult(event: Event, entrant: Person, age: Int, team: String, place: String, seedTime: String, finalTime: String) {
  def mapToRecord(): Result = {
    Result.createRecord.meet(event.meetName).event(event.name).age(age).team(team).seedTime(seedTime).finalTime(finalTime)
  }
}
