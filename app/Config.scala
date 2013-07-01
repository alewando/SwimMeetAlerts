package app

import play.api.Play
import akka.actor.ActorSystem
import grizzled.slf4j.Logging
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
