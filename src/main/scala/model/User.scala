package model

import webapp.lib.{MetaMegaProtoUser, MegaProtoUser}
import xml.Elem
import net.liftweb.mongodb.record.field.MongoListField

class User extends MegaProtoUser[User] {
  def meta = User

  object watching extends MongoListField[User, Swimmer](this)
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
