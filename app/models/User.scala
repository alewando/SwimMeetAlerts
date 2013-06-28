package models

import play.api.Play.current
import java.util.Date
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._
import mongoContext._
import play.Logger

case class User(
  id: ObjectId = new ObjectId,
  email: String,
  password: Password,
  firstName: String,
  lastName: String,
  superUser: Option[Boolean] = None,
  watching: List[ObjectId]
) {
  def fullName: String = {
    firstName + " " + lastName;
  }
}

case class Password( pwd: String, salt: String)

object User extends ModelCompanion[User, ObjectId] {
  val dao = new SalatDAO[User, ObjectId](collection = mongoCollection("users")) {}

  def findOneByUsername(username: String): Option[User] = {
    Logger.info("Searching for user "+ username);
    dao.findOne(MongoDBObject("email" -> username))
  }
}