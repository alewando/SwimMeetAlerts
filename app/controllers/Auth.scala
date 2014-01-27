package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import views._
import models._
import jp.t2v.lab.play2.auth.LoginLogout
import com.typesafe.scalalogging.slf4j.Logging
import auth.AuthenticationConfig
import jp.t2v.lab.play2.auth.AuthElement

object Auth extends Controller with Logging with LoginLogout with AuthenticationConfig {

  val loginForm = Form {
    mapping(
      "email" -> text,
      "password" -> text)(User.authenticate)(_.map(u => (u.email, "")))
      .verifying("Invalid email or password", result => result.isDefined)
  }

  //  def login = Action { implicit request =>
  //    Ok(html.login(loginForm))
  //  }

  implicit def header: HeaderData = HeaderData(Nil)

  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      //formWithErrors => BadRequest(html.login(formWithErrors)),
      formWithErrors => BadRequest(html.meetList()),
      user => gotoLoginSucceeded(user.get.email))
  }

  def logout = Action { implicit request =>
    // do something...
    gotoLogoutSucceeded
  }

}

