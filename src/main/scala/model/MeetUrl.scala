package model

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.{DateField, StringPk}
import net.liftweb.record.field.BooleanField
import java.util.Date

class MeetUrl private() extends MongoRecord[MeetUrl] with StringPk[MeetUrl] {
  def meta = MeetUrl

  object lastCompleted extends DateField[MeetUrl](this) {
    // Default to "Low" date
    override def defaultValue = new Date(0)
  }
  
  object inProgress extends BooleanField[MeetUrl](this)

  // TODO: Check for trailing slash
  def eventIndexUrl = id.is + "/evtindex.htm"

}

object MeetUrl extends MeetUrl with MongoMetaRecord[MeetUrl] {

}
