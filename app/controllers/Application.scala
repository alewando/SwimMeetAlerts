package controllers

import play.api._
import play.api.mvc._
import jp.t2v.lab.play2.auth.AuthElement
import auth.AuthenticationConfig
import auth.NormalUser

object Application extends Controller with AuthElement with AuthenticationConfig {

  def index = StackAction(AuthorityKey -> NormalUser) { implicit request => Ok(views.html.index(loggedIn)) };

}