package actors

import model.{MeetUrl, Result}

case class ScrapeAllMeets()

case class ScrapeMeet(url: MeetUrl)

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
    Result.createRecord.meet(event.meetName).event(event.name).place(place).age(age).team(team).seedTime(seedTime).finalTime(finalTime)
  }
}
