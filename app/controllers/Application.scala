package controllers

import play.api._
import play.api.mvc._

object Application extends Controller with Secured {
  
//  def index_orig = Action {
//    Ok(views.html.index("Your new application is ready, Freddy and Betty"))  
//  }
  
  def index = withUser { user => implicit request => Ok(views.html.index(user)) };
  
}