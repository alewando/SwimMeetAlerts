package actors

import org.slf4j.LoggerFactory
import play.api.Play.current
import play.api.libs.concurrent.Akka

/**
 * Trait for sending notifications to the system admin
 */

trait AdminNotifier {

  val adminNotifier = Akka.system.actorFor("/user/emailSender")

  def sendAdminEmail(subject: String, message: String) {
    LoggerFactory.getLogger(this.getClass).info("Sending admin email: {}", subject)
    adminNotifier ! AdminMessage(subject, message)
  }

}

case class AdminMessage(subject: String, message: String)

