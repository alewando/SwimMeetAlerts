package model

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.{DateField, StringPk}
import java.util.Date
import net.liftweb.record.field.{StringField, BooleanField}
import net.liftweb.mongodb.BsonDSL._


class MeetUrl private() extends MongoRecord[MeetUrl] with StringPk[MeetUrl] {
  def meta = MeetUrl

  object lastCompleted extends DateField[MeetUrl](this) {
    // Default to "Low" date
    override def defaultValue = new Date(0)
  }

  object inProgress extends BooleanField[MeetUrl](this)

  object name extends StringField[MeetUrl](this, 100) {
    override def defaultValue = ""
  }

  // TODO: Check for trailing slash
  def eventIndexUrl = id.is + "/evtindex.htm"

}

object MeetUrl extends MeetUrl with MongoMetaRecord[MeetUrl] {

  def inProgressMeets: List[MeetUrl] = {
    findAll(("inProgress" -> true))
  }
}
