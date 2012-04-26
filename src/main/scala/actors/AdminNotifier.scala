package actors

import akka.actor.Actor

/**
 * Trait for sending notifications to the system admin
 */

trait AdminNotifier extends Actor {

  val adminNotifier = context.actorFor("/user/emailSender")

  def sendAdminEmail(subject: String, message: String) {
    adminNotifier ! AdminMessage(subject, message)
  }

}

case class AdminMessage(subject: String, message: String)

