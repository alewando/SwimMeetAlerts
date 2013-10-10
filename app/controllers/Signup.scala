package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import views._
import models._
import grizzled.slf4j.Logging

case class SignupInfo(first: String, last: String, email: String, password: String)

object Signup extends Controller with Logging {

  val signupForm: Form[SignupInfo] = Form {
    mapping("first" -> text, "last" -> text, "email" -> email, "password" -> text)(SignupInfo.apply)(SignupInfo.unapply)
  }

  def form = Action { implicit request =>
    Ok(html.signup(signupForm))
  }

  /**
   * Handle form submission.
   */
  def submit = Action { implicit request =>
    signupForm.bindFromRequest.fold(
      errors => BadRequest(html.signup(errors)),
      info => {
        // Save the new user
        User.create(info.first, info.last, info.email, info.password)
        Ok(html.meetList())
      })
  }

}

