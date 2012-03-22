package model

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.{DateField, StringPk}
import net.liftweb.record.field.BooleanField

class MeetUrl private() extends MongoRecord[MeetUrl] with StringPk[MeetUrl] {
  def meta = MeetUrl

  object lastModified extends DateField[MeetUrl](this)

  object completed extends BooleanField[MeetUrl](this)

}

object MeetUrl extends MeetUrl with MongoMetaRecord[MeetUrl] {

}
