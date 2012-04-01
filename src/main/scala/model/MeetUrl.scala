package model

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.{DateField, StringPk}
import java.util.Date
import net.liftweb.record.field.{StringField, BooleanField}
import net.liftweb.mongodb.BsonDSL._

import dispatch._
import org.slf4j.LoggerFactory


class MeetUrl private() extends MongoRecord[MeetUrl] with StringPk[MeetUrl] {
  def meta = MeetUrl

  val log = LoggerFactory.getLogger(getClass)

  def eventIndexPage = "evtindex.htm"

  object lastCompleted extends DateField[MeetUrl](this) {
    // Default to "Low" date
    override def defaultValue = new Date(0)
  }

  object inProgress extends BooleanField[MeetUrl](this)

  object name extends StringField[MeetUrl](this, 100) {
    override def defaultValue = ""
  }

  def eventIndexUrl = id.is match {
    case x if x.endsWith("/") => id.is + eventIndexPage
    case _ => id.is + "/" + eventIndexPage
  }

  def validMeetUrl_? : Boolean = {
    // Look for an event index page
    try {
      val u = url(eventIndexUrl)
      Http(u >:> identity)
      log.debug("URL " + eventIndexUrl + " passes")
      true
    } catch {
      case _ => {
        log.info("Not a valid meet URL: {}", eventIndexUrl)
        false
      }
    }
  }

}

object MeetUrl extends MeetUrl with MongoMetaRecord[MeetUrl] {

  def inProgressMeets: List[MeetUrl] = {
    findAll(("inProgress" -> true))
  }

}
