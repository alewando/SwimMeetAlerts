package model

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.record.field.{StringField, IntField}
import xml.NodeSeq

class Result private() extends MongoRecord[Result] with ObjectIdPk[Result] {
  def meta = Result

  object meet extends StringField(this, 100)

  object event extends StringField(this, 100)

  object age extends IntField(this)

  object team extends StringField(this, 100)

  object place extends StringField(this, 10)

  object seedTime extends StringField(this, 50)

  object finalTime extends StringField(this, 50)

  def toShortHtml: NodeSeq = {
    <div class="result">
      <span class="eventName">
        {this.event.is}
      </span>
      :
      <span class="resultTime">
        {this.finalTime.is}
      </span>
    </div>
  }

}

object Result extends Result with MongoMetaRecord[Result]
