package model

import webapp.lib.{MetaMegaProtoUser, MegaProtoUser}
import net.liftweb.mongodb.record.field.MongoListField
import org.bson.types.ObjectId
import actors.AdminNotifier
import com.mongodb.WriteConcern

class User extends MegaProtoUser[User] with AdminNotifier {
  def meta = User

  object watching extends MongoListField[User, ObjectId](this)

  def allWatchedSwimmers: List[Swimmer] = {
    for (swimmerId <- watching.value;
         swimmer <- Swimmer.find(swimmerId)
    ) yield {
      swimmer
    }
  }

  override def save(concern: WriteConcern) = {
    sendAdminEmail("New user", "New user signup: %s".format(this.niceName))
    super.save(concern)
  }
}

object User extends User with MetaMegaProtoUser[User] {
  override def skipEmailValidation = true

  override def loginXhtml =
    <lift:surround with="default" at="content">
      {super.loginXhtml}
    </lift:surround>

  // Provide our own signup page template.
  override def signupXhtml(user: User) =
    <lift:surround with="default" at="content">
      {super.signupXhtml(user)}
    </lift:surround>

  override def lostPasswordXhtml =
    <lift:surround with="default" at="content">
      {super.lostPasswordXhtml}
    </lift:surround>
}
