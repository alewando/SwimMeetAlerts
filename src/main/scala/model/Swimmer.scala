package model

import net.liftweb.mongodb.record.{BsonMetaRecord, BsonRecord, MongoMetaRecord, MongoRecord}
import net.liftweb.record.field.StringField
import net.liftweb.mongodb.record.field.{BsonRecordField, MongoListField, ObjectIdPk}
import scraper.{Person}

class Swimmer private() extends MongoRecord[Swimmer] with ObjectIdPk[Swimmer] {
  def meta = Swimmer

  object name extends BsonRecordField(this, Name)

  object watchers extends MongoListField[Swimmer, User](this)

}

object Swimmer extends Swimmer with MongoMetaRecord[Swimmer] {
  def findForResult(result: scraper.Result) = {
    //TODO: Implement serach by result first/last name
    result match {
      case scraper.Result(_, Person("Jordan", "Carman"), _, _, _, _, _) => Swimmer.find(
        "{name: {firstName: 'Jordan', lastName: 'Carman'}}") getOrElse null
      case _ => null
    }
    null
  }
}

class Name extends BsonRecord[Name] {
  def meta = Name

  object firstName extends StringField(this, 100)

  object middleName extends StringField(this, 100)

  object lastName extends StringField(this, 100)

  // TODO: Middle initial

}

object Name extends Name with BsonMetaRecord[Name]