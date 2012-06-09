package model

import webapp.lib.{MetaMegaProtoUser, MegaProtoUser}
import net.liftweb.mongodb.record.field.MongoListField
import org.bson.types.ObjectId
import actors.AdminNotifier
import com.mongodb.WriteConcern
import net.liftweb.common.Empty

class User extends MegaProtoUser[User] with AdminNotifier {
  def meta = User

  object watching extends MongoListField[User, ObjectId](this)

  object alertDestinations extends MongoListField[User, String](this)

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

  override def signupFields: List[FieldPointerType] = List(firstName, lastName, email, password)

  override def skipEmailValidation = true

  // Disable menu item for editing user info
  override def editUserMenuLoc = Empty

  // Disable menu item for changing password
  override def changePasswordMenuLoc = Empty

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

  override def changePasswordXhtml =
    <lift:surround with="default" at="content">
      {super.changePasswordXhtml}
    </lift:surround>
}
