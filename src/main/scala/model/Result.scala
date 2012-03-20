package model

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.record.field.{StringField, IntField}

class Result private() extends MongoRecord[Result] with ObjectIdPk[Result] {
  def meta = Result

  object meet extends StringField(this, 100)

  object event extends StringField(this, 100)

  object age extends IntField(this)

  object team extends StringField(this, 100)

  object place extends StringField(this, 10)

  object seedTime extends StringField(this, 50)

  object finalTime extends StringField(this, 50)

}

object Result extends Result with MongoMetaRecord[Result]
