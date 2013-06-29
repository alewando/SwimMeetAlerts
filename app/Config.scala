package app

import play.api.Play
import akka.actor.ActorSystem
import akka.actor.Props
import actors.ResultProcessor
import actors.EmailSender
import actors.Driver
import akka.routing.RoundRobinRouter

object Config {
  val BASE_URL = Play.current.configuration.getString("meet.baseUrl") getOrElse ""
  val SMTP_USER = Play.current.configuration.getString("sendgrid.username") getOrElse ""
  val SMTP_PASSWORD = Play.current.configuration.getString("sendgrid.password") getOrElse ""
  val SMTP_SERVER = Play.current.configuration.getString("smtp.server") getOrElse ""
  val EMAIL_FROM_ADDRESS = Play.current.configuration.getString("email.from.address") getOrElse ""
  val ADMIN_EMAIL = Play.current.configuration.getString("admin.email") getOrElse ""
}

object Actors {
  val actors: ActorSystem = initActors

  def get: ActorSystem = actors

  def initActors = {
    val system = ActorSystem("AlertsSystem")
    system.actorOf(Props[EmailSender], name = "emailSender")
    system.actorOf(Props[ResultProcessor].withRouter(RoundRobinRouter(15)), name = "resultProcessor")
    system.actorOf(Props[Driver], name = "driver")
    system
  }
}