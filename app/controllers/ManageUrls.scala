package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._

import views._
import models._
import grizzled.slf4j.Logging

case class NewMeetUrl(url: String)

object ManageUrls extends Controller with Logging {

  val addUrlForm: Form[NewMeetUrl] = Form(
    mapping("url" -> text)(NewMeetUrl.apply)(NewMeetUrl.unapply))

  def form = Action { implicit request =>
    Ok(html.manageUrls(addUrlForm))
  }

  /**
   * Handle form submission.
   */
  def add = Action { implicit request =>
    addUrlForm.bindFromRequest.fold(
      errors => BadRequest(html.manageUrls(errors)),
      newUrl => {
        Meet.save(new Meet(id = newUrl.url, name = ""))
        Ok(html.manageUrls(addUrlForm))
      })
  }

}

