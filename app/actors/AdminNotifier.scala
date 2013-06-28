package actors

import webapp.WebApp
import org.slf4j.LoggerFactory


/**
 * Trait for sending notifications to the system admin
 */

trait AdminNotifier {

  val adminNotifier = WebApp.actors.actorFor("/user/emailSender")

  def sendAdminEmail(subject: String, message: String) {
    LoggerFactory.getLogger(this.getClass).info("Sending admin email: {}", subject)
    adminNotifier ! AdminMessage(subject, message)
  }

}

case class AdminMessage(subject: String, message: String)

