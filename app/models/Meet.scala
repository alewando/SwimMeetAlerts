package models

import play.api.Play.current
import java.util.Date
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._
import mongoContext._
import play.Logger
import dispatch.classic._
import com.typesafe.scalalogging.slf4j.Logging

case class Meet(
  @Key("_id") id: String,
  name: String,
  inProgress: Boolean = false,
  lastCompleted: Option[Date]) extends Logging {
  def eventIndexPage = "evtindex.htm"

  def eventIndexUrl = id match {
    case x if x.endsWith("/") => id + eventIndexPage
    case _ => id + "/" + eventIndexPage
  }

  def validMeetUrl_? : Boolean = {
    // Look for an event index page
    try {
      val u = url(eventIndexUrl)
      Http(u >:> identity)
      logger.debug(s"URL ${eventIndexUrl} passes")
      true
    } catch {
      case _: Throwable => {
        logger.info(s"Not a valid meet URL: ${eventIndexUrl}")
        false
      }
    }
  }
}

object Meet extends ModelCompanion[Meet, String] {
  val dao = new SalatDAO[Meet, String](collection = mongoCollection("meeturls")) {}

  def inProgress: List[Meet] = {
    dao.find(MongoDBObject("inProgress" -> true)).toList
  }
}

case class CompletedEvent(
  id: ObjectId = new ObjectId,
  meetName: String,
  eventUrl: String) {
}

object CompletedEvent extends ModelCompanion[CompletedEvent, ObjectId] {
  val dao = new SalatDAO[CompletedEvent, ObjectId](collection = mongoCollection("completedevents")) {}

  def eventCompleted_?(meetName: String, eventUrl: String): Boolean = {
    dao.find(MongoDBObject("meetName" -> meetName, "eventUrl" -> eventUrl)).hasNext
  }

  def markEventComplete(meetName: String, eventUrl: String) {
    CompletedEvent.save(new CompletedEvent(meetName = meetName, eventUrl = eventUrl))
  }

  /**
   * Remove all CompletedEvent records for the specified meet
   */
  def deleteEventsForMeet(meetName: String) {
    dao.find(MongoDBObject("meetName" -> meetName)) map (CompletedEvent.remove(_))
  }
}