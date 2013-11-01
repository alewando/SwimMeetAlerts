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
import org.mindrot.jbcrypt.BCrypt
import com.typesafe.scalalogging.slf4j.Logging

case class User(
  id: ObjectId = new ObjectId,
  email: String,
  pw: String,
  firstName: String,
  lastName: String,
  superUser: Option[Boolean] = None,
  watching: List[ObjectId] = Nil,
  extraDestinations: List[String] = Nil) {
  def fullName: String = {
    firstName + " " + lastName;
  }
}

object User extends ModelCompanion[User, ObjectId] with Logging {
  val dao = new SalatDAO[User, ObjectId](collection = mongoCollection("users")) {}

  def findOneByUsername(username: String): Option[User] = {
    dao.findOne(MongoDBObject("email" -> username))
  }

  def authenticate(username: String, password: String): Option[User] = {
    findOneByUsername(username) filter { account => BCrypt.checkpw(password, account.pw) }
  }

  def create(firstName: String, lastName: String, email: String, pw: String) {
    //import account._
    val pass = BCrypt.hashpw(pw, BCrypt.gensalt())
    dao.save(new User(firstName = firstName, lastName = lastName, email = email, pw = pass))
  }

}