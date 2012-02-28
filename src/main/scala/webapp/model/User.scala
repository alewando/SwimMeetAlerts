package webapp.model

import webapp.lib.{MetaMegaProtoUser, MegaProtoUser}

class User extends MegaProtoUser[User] {
  def meta = User

}

object User extends User with MetaMegaProtoUser[User] {
  override def skipEmailValidation = true
}
