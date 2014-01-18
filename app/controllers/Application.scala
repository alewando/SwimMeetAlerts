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

object Application extends Controller with AuthElement with AuthenticationConfig {

  def javascriptRoutes = Action { implicit request =>
    import routes.javascript._
    Ok(
      Routes.javascriptRouter("jsRoutes")(routes.javascript.Application.getFollowedSwimmers, routes.javascript.ManageSwimmers.unfollow)).as("text/javascript")
  }

  def index = StackAction(AuthorityKey -> NormalUser) { implicit request => Ok(html.index()) };

  def meetlist = Action { implicit request => Ok(html.meetList()) }

  def getFollowedSwimmers = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    logger.info("GETTING SWIMMERS!")

    val watched = loggedIn.watching.map { id =>
      Swimmer.findById(id) // map { s => s.name.firstName + " " + s.name.lastName }
    }.flatten

    val js: List[JsObject] = watched map { s =>
      val name = s.name.firstName + " " + s.name.lastName
      Json.obj("name" -> name, "results" -> Json.arr(s.results))
    }
    Ok(Json.arr(js))
  }

}