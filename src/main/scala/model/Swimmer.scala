package model

import net.liftweb.mongodb.record.{BsonMetaRecord, BsonRecord, MongoMetaRecord, MongoRecord}
import net.liftweb.record.field.StringField
import org.slf4j.LoggerFactory
import net.liftweb.mongodb.BsonDSL._
import org.bson.types.ObjectId
import net.liftweb.mongodb.record.field.{ObjectIdRefField, BsonRecordField, MongoListField, ObjectIdPk}

class Swimmer private() extends MongoRecord[Swimmer] with ObjectIdPk[Swimmer] {
  def meta = Swimmer

  val log = LoggerFactory.getLogger(this.getClass)

  object name extends BsonRecordField(this, Name)

  object watchers extends MongoListField[Swimmer, ObjectId](this)

  def addWatcher(user: User) {
    Swimmer.update(("_id" -> this.id.is), ("$addToSet" ->("watchers", user.id.asInstanceOf[ObjectId])))
    val upd = ("$addToSet" ->("watching", this.id.is.asInstanceOf[ObjectId]))
    User.update(("_id" -> user.id), upd)
    log.info("User {} now watching swimmer: {}", user.shortName, this)
  }

}

object Swimmer extends Swimmer with MongoMetaRecord[Swimmer] {
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