package app

import play.api.Play
import akka.actor.ActorSystem
import akka.actor.Props
import actors.ResultProcessor
import actors.EmailSender
import actors.Driver
import akka.routing.RoundRobinRouter

object Config {
  def BASE_URL = Play.current.configuration.getString("meet.baseUrl") getOrElse ""
  def SMTP_USER = Play.current.configuration.getString("sendgrid.username") getOrElse ""
  def SMTP_PASSWORD = Play.current.configuration.getString("sendgrid.password") getOrElse ""
  def SMTP_SERVER = Play.current.configuration.getString("smtp.server") getOrElse ""
  def EMAIL_FROM_ADDRESS = Play.current.configuration.getString("email.from.address") getOrElse "alert@swimmeetalerts.com"
  def ADMIN_EMAIL = Play.current.configuration.getString("admin.email") getOrElse "adam@alewando.com"
}
