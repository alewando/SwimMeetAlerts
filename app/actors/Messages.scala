package actors

import models.{ Meet, EventResult }
import org.joda.time.format.PeriodFormatterBuilder
import org.joda.time.Period
import models.EventResult

case class ScrapeAllMeets()

case class ScrapeMeet(url: Meet)

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
  def mapToRecord(): EventResult = {
    //TODO: Move delta calculation to ResultProcessor
    // Calculate time delta
    val Time = """((\d+):)?(\d+).(\d+)""".r
    def parse(ts: String) = ts match {
      case Time(_, min, sec, ms) => Some(new Period(0, if (min != null) min.toInt else 0, sec.toInt, ms.toInt).toStandardDuration)
      case _ => None
    }
    val seed = parse(seedTime)
    val fin = parse(finalTime)
    val delta = (seed, fin) match {
      case (Some(s), Some(f)) if f.compareTo(s) > 0 => Some(("+", f minus s))
      case (Some(s), Some(f)) if f.compareTo(s) <= 0 => Some(("-", s minus f))
      case _ => None
    }
    val fmt = new PeriodFormatterBuilder().printZeroNever().appendMinutes().appendSeparator(":").printZeroAlways().appendSeconds().appendSeparator(".").appendMillis().toFormatter

    EventResult(meet = event.meetName, event = event.name, place = place, age = age, team = team, seedTime = seedTime, finalTime = finalTime, delta = delta match {
      case Some((sign, diff)) => Some(sign + fmt.print(diff.toPeriod))
      case _ => None
    })
  }
}
