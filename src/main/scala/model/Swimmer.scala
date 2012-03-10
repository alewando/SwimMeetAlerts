package model

import net.liftweb.mongodb.record.{BsonMetaRecord, BsonRecord, MongoMetaRecord, MongoRecord}
import net.liftweb.record.field.StringField
import net.liftweb.mongodb.record.field.{BsonRecordField, MongoListField, ObjectIdPk}
import org.slf4j.LoggerFactory
import net.liftweb.mongodb.BsonDSL._

class Swimmer private() extends MongoRecord[Swimmer] with ObjectIdPk[Swimmer] {
  def meta = Swimmer

  object name extends BsonRecordField(this, Name)

  object watchers extends MongoListField[Swimmer, User](this)

}

object Swimmer extends Swimmer with MongoMetaRecord[Swimmer] {
  val log = LoggerFactory.getLogger(this.getClass)

  def findForResult(result: scraper.ScrapedResult) = {
    Swimmer.find(("name.firstName" -> result.entrant.firstName) ~ ("name.lastName" -> result.entrant.lastName))
  }
}

class Name extends BsonRecord[Name] {
  def meta = Name

  object firstName extends StringField(this, 100)

  object middleName extends StringField(this, 100) {
    override def optional_? = true
  }

  object lastName extends StringField(this, 100)

  // TODO: Middle initial

  def fullName: String = {
    firstName + " " + lastName;
  }

}

object Name extends Name with BsonMetaRecord[Name]