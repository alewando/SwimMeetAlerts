package controllers

import play.api._
import play.api.mvc._
import jp.t2v.lab.play2.auth.AuthElement
import auth.AuthenticationConfig
import auth.NormalUser
import views._
import models.Swimmer
import models.Swimmer._
import play.api.libs.json._
import models.EventResult
import com.mongodb.casbah.Imports._
import models.Meet

object Application extends Controller with AuthElement with AuthenticationConfig {

  def javascriptRoutes = Action { implicit request =>
    import routes.javascript._
    Ok(
      Routes.javascriptRouter("jsRoutes")(
        routes.javascript.Application.getFollowedSwimmers,
        routes.javascript.Application.activeMeets,
        routes.javascript.ManageSwimmers.unfollow)).as("text/javascript")
  }

  def index = StackAction(AuthorityKey -> NormalUser) { implicit request => Ok(html.index()) };

  def meetlist = Action { implicit request => Ok(html.meetList()) }

  def getFollowedSwimmers = StackAction(AuthorityKey -> NormalUser) { implicit request =>

    val watched = loggedIn.watching.map { id =>
      Swimmer.findById(id) // map { s => s.name.firstName + " " + s.name.lastName }
    }.flatten

    val js: List[JsObject] = watched map { s =>
      val name = s.name.firstName + " " + s.name.lastName
      val eventsByMeet = for ((meet, events) <- s.eventsByMeet) yield {
        Json.obj("meet" -> meet, "events" -> events)
      }
      Json.obj("name" -> name, "id" -> s.id, "results" -> eventsByMeet)
    }

    Ok(Json.toJson(js))
  }

  def activeMeets = Action { implicit request =>
    val activeMeets = Meet.inProgress map { x => Json.obj("name" -> x.name, "url" -> x.eventIndexUrl) }
    Ok(Json.toJson(activeMeets))
  }

}