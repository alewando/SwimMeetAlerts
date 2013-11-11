package controllers

import com.typesafe.scalalogging.slf4j.Logging
import actors.ScrapeAllMeets
import actors.ScrapeMeet
import akka.actor.actorRef2Scala
import models.Meet
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms.tuple
import play.api.data.Forms.text
import play.api.libs.concurrent.Akka
import play.api.mvc.Action
import play.api.mvc.Controller
import views.html
import jp.t2v.lab.play2.auth.AuthElement
import auth.AuthenticationConfig
import auth.Administrator
import auth.NormalUser
import models._

object ManageSwimmers extends Controller with AuthElement with AuthenticationConfig with Logging {

  val addSwimmerForm = Form(tuple("firstName" -> text, "lastName" -> text))

  def add = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    val (firstName, lastName) = addSwimmerForm.bindFromRequest.get
    logger.info(s"Adding Swimmer: ${firstName} ${lastName}");

    // Find the requested Swimmer by name, adding if they don't exist
    val swimmer = Swimmer.findForName(s"${firstName} ${lastName}") match {
      case Some(swimmer) => Some(swimmer)
      case _ => Swimmer.insert(Swimmer(name = Name(firstName, lastName))) match {
        case Some(id) => Swimmer.findById(id)
        case _ => None
      }
    }

    // Add current user to swimmers 'watchers' list
    swimmer map { _.addWatcher(loggedIn) }

    Ok(html.index())
  }

}

