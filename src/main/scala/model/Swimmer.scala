package model

import net.liftweb.mongodb.record.{BsonMetaRecord, BsonRecord, MongoMetaRecord, MongoRecord}
import org.slf4j.LoggerFactory
import net.liftweb.mongodb.BsonDSL._
import org.bson.types.ObjectId
import net.liftweb.mongodb.record.field.{BsonRecordField, MongoListField, ObjectIdPk}
import net.liftweb.record.field.{OptionalStringField, StringField}
import net.liftweb.common.{Box, Empty, Full}

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
  val Name = """(\w+) (\w){1} (\w+)""".r

  def findForResult(result: scraper.ScrapedResult) : Box[Swimmer] = {
    // Search by upper-cased name
    val searchName = result.entrant.fullName.toUpperCase
    //log.debug("Searching for {}",searchName)
    Swimmer.find("name.searchName" -> searchName) match {
      case Full(x) => Full(x)
      case _ => {
        searchName match {
          case Name(first, mi, last) => {
            //log.debug("Searching for name w/o MI: {} {}", first, last)
            Swimmer.find("name.searchName" -> first + " " + last)
          }
          case _ => Empty
        }
      }
    }
  }
}

class Name extends BsonRecord[Name] {
  def meta = Name

  object firstName extends StringField(this, 100)

  object lastName extends StringField(this, 100)

  object searchName extends StringField(this, 200) {
    override def defaultValue = fullName.toUpperCase
  }

  def fullName: String = {
    firstName + " " + lastName;
  }

}

object Name extends Name with BsonMetaRecord[Name]