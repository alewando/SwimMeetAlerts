package models

import play.api.Play.current

import com.mongodb.casbah.Imports._
import com.novus.salat.dao.SalatDAO
import com.novus.salat.dao.ModelCompanion
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._
import mongoContext._
import play.Logger

case class Swimmer(
  id: ObjectId = new ObjectId,
  name: Name,
  watchers: List[ObjectId],
  results: List[EventResult],
  aliasFor: Option[ObjectId]) {
  def fullName: String = {
    name.firstName + " " + name.lastName;
  }
}

object Swimmer extends ModelCompanion[Swimmer, ObjectId] {
  val dao = new SalatDAO[Swimmer, ObjectId](collection = mongoCollection("swimmers")) {}

  val NamePattern = """(\w+) (\w){1} (\w+)""".r

  def findById(id: ObjectId): Option[Swimmer] = {
    dao.findOne(MongoDBObject("_id" -> id))
  }

  def findForName(fullName: String): Option[Swimmer] = {
    // Search by upper-cased name
    val searchName = fullName.toUpperCase
    log.trace("Searching for {}", searchName)
    val res = findOne(MongoDBObject("name.searchName" -> searchName)) match {
      case Some(x) => Some(x)
      case _ => {
        searchName match {
          case NamePattern(first, mi, last) => {
            log.trace("Searching for name w/o MI: \"{} {}\"", first, last)
            findOne(MongoDBObject("name.searchName" -> (first + " " + last)))
          }
          case _ => None
        }
      }
    }
    // Resolve 'alias' record to their referenced swimmer record
    res map {
      found =>
        found.aliasFor match {
          case Some(id) => Swimmer.findById(id) getOrElse found
          case _ => found
        }
    }
  }

}

case class Name(firstName: String, lastName: String, searchName: String)

case class EventResult(
  id: ObjectId,
  meet: String,
  event: String,
  age: Int,
  team: String,
  place: String,
  seedTime: String,
  finalTime: String,
  delta: Option[String])

