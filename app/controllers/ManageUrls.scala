package controllers

import com.typesafe.scalalogging.slf4j.Logging
import actors.ScrapeAllMeets
import actors.ScrapeMeet
import akka.actor.actorRef2Scala
import models.Meet
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms.text
import play.api.libs.concurrent.Akka
import play.api.mvc.Action
import play.api.mvc.Controller
import views.html
import jp.t2v.lab.play2.auth.AuthElement
import auth.AuthenticationConfig
import auth.Administrator

case class NewMeetUrl(url: String)

object ManageUrls extends Controller with AuthElement with AuthenticationConfig with ProvidesHeader with Logging {

  val addUrlForm: Form[NewMeetUrl] = Form(
    mapping("url" -> text)(NewMeetUrl.apply)(NewMeetUrl.unapply))

  def form = StackAction(AuthorityKey -> Administrator) { implicit request =>
    Ok(html.manageUrls(addUrlForm))
  }

  def add = StackAction(AuthorityKey -> Administrator) { implicit request =>
    addUrlForm.bindFromRequest.fold(
      errors => BadRequest(html.manageUrls(errors)),
      newUrl => {
        logger.info(s"Adding URL: ${newUrl.url}");
        Meet.save(new Meet(id = newUrl.url, name = "", lastCompleted = None))
        Ok(html.manageUrls(addUrlForm))
      })
  }

  def removeUrl(meetUrl: String) = StackAction(AuthorityKey -> Administrator) { implicit request =>
    logger.info(s"Removing URL: ${meetUrl}")
    Meet.findOneById(meetUrl) map { Meet.remove(_) }
    Ok(html.manageUrls(addUrlForm))
  }

  def scrapeAll = StackAction(AuthorityKey -> Administrator) { implicit request =>
    Akka.system.actorFor("user/driver") ! ScrapeAllMeets
    Ok(html.manageUrls(addUrlForm))
  }

  def scrape(url: String) = StackAction(AuthorityKey -> Administrator) { implicit request =>
    logger.info(s"Request to scrape ${url}")
    Meet.findOneById(url).map { meet =>
      logger.info(s"Scraping meet: ${meet}")

      Akka.system.actorFor("user/driver") ! ScrapeMeet(meet)
      Ok(html.manageUrls(addUrlForm))
    }.getOrElse(NotFound)
  }

}

