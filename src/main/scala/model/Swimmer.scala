package model

import net.liftweb.mongodb.record.{BsonMetaRecord, BsonRecord, MongoMetaRecord, MongoRecord}
import org.slf4j.LoggerFactory
import net.liftweb.mongodb.BsonDSL._
import org.bson.types.ObjectId
import net.liftweb.record.field.StringField
import net.liftweb.common.{Box, Empty, Full}
import actors.ScrapedResult
import net.liftweb.mongodb.record.field.{ObjectIdField, BsonRecordListField, BsonRecordField, MongoListField, ObjectIdPk}

class Swimmer private() extends MongoRecord[Swimmer] with ObjectIdPk[Swimmer] {
  def meta = Swimmer

  val log = LoggerFactory.getLogger(this.getClass)

  object name extends BsonRecordField(this, Name)

  object watchers extends MongoListField[Swimmer, ObjectId](this)

  object results extends BsonRecordListField(this, Result)

  // If this field has a value, this record is just a reference to another swimmer record. 
  // It exists only so that a swimmer can be found be searching on a nickname/alias (ie: 'Joe' for 'Joseph')
  object aliasFor extends ObjectIdField[Swimmer](this) {
    override def optional_? = true

    override def defaultValueBox = Empty
  }

  def addWatcher(user: User) {
    Swimmer.update(("_id" -> this.id.is), ("$addToSet" ->("watchers", user.id.asInstanceOf[ObjectId])))
    val upd = ("$addToSet" ->("watching", this.id.is.asInstanceOf[ObjectId]))
    User.update(("_id" -> user.id), upd)
    log.info("User {} now watching swimmer: {}", user.shortName, this)
  }

  def removeWatcher(user: User) {
    Swimmer.update(("_id" -> this.id.is), ("$pull" ->("watchers", user.id.asInstanceOf[ObjectId])))
    User.update(("_id" -> user.id.asInstanceOf[ObjectId]), ("$pull" ->("watching", this.id.is)))
    log.info("User {} is no longer watching swimmer: {}", user.shortName, this)
  }


  def addResult(result: Result) {
    Swimmer.update(("_id" -> this.id.is), ("$push" ->("results", result.asJValue)))
  }

  def resultExists_?(candidate: ScrapedResult): Boolean = {
    Swimmer.find(
      ("results" -> ("$elemMatch" -> ("meet" -> candidate.event.meetName) ~ ("event" -> candidate.event.name)))
    ).isDefined
  }

}

object Swimmer extends Swimmer with MongoMetaRecord[Swimmer] {
  val NamePattern = """(\w+) (\w){1} (\w+)""".r

  def createForName(firstName: String, lastName: String): Swimmer = {
    createRecord.name(Name.createRecord.firstName(firstName).lastName(lastName)).save
  }

  def findForName(fullName: String): Box[Swimmer] = {
    // Search by upper-cased name
    val searchName = fullName.toUpperCase
    log.trace("Searching for {}", searchName)
    val res = find("name.searchName" -> searchName) match {
      case Full(x) => Full(x)
      case _ => {
        searchName match {
          case NamePattern(first, mi, last) => {
            log.trace("Searching for name w/o MI: \"{} {}\"", first, last)
            find("name.searchName" -> (first + " " + last))
          }
          case _ => Empty
        }
      }
    }
    // TODO: Support 'alias' records, containing a pointer to another swimmer record
    res
  }

  def findForResult(result: ScrapedResult): Box[Swimmer] = {
    findForName(result.entrant.fullName)
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