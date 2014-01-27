package controllers

import com.typesafe.scalalogging.slf4j.Logging
import akka.actor.actorRef2Scala
import auth.AuthenticationConfig
import auth.NormalUser
import jp.t2v.lab.play2.auth.AuthElement
import models._
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms.text
import play.api.data.Forms.tuple
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.mvc.Action
import play.api.mvc.Controller
import views.html
import org.bson.types.ObjectId

object ManageSwimmers extends Controller with AuthElement with AuthenticationConfig with ProvidesHeader with Logging {

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

  implicit val unfollowRds = (__ \ 'swimmerId).read[String]
  def unfollow = StackAction(parse.json, AuthorityKey -> NormalUser) { implicit request =>
    request.body.validate[String].map {
      case (swimmerId) =>
        logger.info("Unfollowing {}", swimmerId)
        Swimmer.findById(new ObjectId(swimmerId)) map { _.removeWatcher(loggedIn) }
        Ok(Json.obj("result" -> "success"))
    }.recoverTotal {
      e => BadRequest("Detected error:" + JsError.toFlatJson(e))
    }
  }
}
