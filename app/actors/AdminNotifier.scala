package actors

import app.Actors
import org.slf4j.LoggerFactory

/**
 * Trait for sending notifications to the system admin
 */

trait AdminNotifier {

  val adminNotifier = Actors.get.actorFor("/user/emailSender")

  def sendAdminEmail(subject: String, message: String) {
    LoggerFactory.getLogger(this.getClass).info("Sending admin email: {}", subject)
    adminNotifier ! AdminMessage(subject, message)
  }

}

case class AdminMessage(subject: String, message: String)

