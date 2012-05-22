package actors

import webapp.Config._
import javax.mail.internet.{InternetAddress, MimeMessage}
import org.slf4j.LoggerFactory
import java.util.Properties
import javax.mail._
import akka.actor.Actor
import model.{Swimmer, Result}


class EmailSender extends Actor {
  val log = LoggerFactory.getLogger(this.getClass)

  val props = new Properties();
  props.put("mail.smtp.host", SMTP_SERVER);
  if (SMTP_USER != null) {
    props.put("mail.smtp.user", SMTP_USER);
  }
  val session = Session.getInstance(props, null);

  def receive = {
    case (swimmer: Swimmer, result: Result, recipients: List[String]) =>
      sendResultEmail(swimmer, result, recipients)
    case AdminMessage(subject, message) => {
      log.info("Sending admin email w/ subject '{}'", subject)
      sendEmail(ADMIN_EMAIL :: Nil, subject, message)
    }
  }

  def sendEmail(to: List[String], subject: String, body: String) {
    // Set up the mail object
    val message = new MimeMessage(session)

    // Set the from, to, subject, body text
    message.setFrom(new InternetAddress(EMAIL_FROM_ADDRESS))

    val recipients: Array[Address] = to.map(new InternetAddress(_)).toArray

    // TODO: Don't use BCC if only one recipient
    message.setRecipients(Message.RecipientType.BCC, recipients)
    message.setSubject(subject)
    message.setText(body)

    // Send the message
    try {
      val trans = session.getTransport("smtp")
      trans.connect(SMTP_SERVER, 25, SMTP_USER, SMTP_PASSWORD)
      trans.sendMessage(message, message.getAllRecipients())
      trans.close()
    } catch {
      case e =>
        log.error("send failed, exception: " + e);
    }

  }

  def sendResultEmail(swimmer: Swimmer, result: Result, recipientAddresses: List[String]) {
    log.info("Sending email to {} for result {}", recipientAddresses, result)

    // TODO: Calculate delta between seed and final times (use joda Period to parse, convert to Duration for calc)

    val subject = "New Result for " + swimmer.name.value.fullName + ": " + result.event.is
    val body = swimmer.name.value.fullName + "\n" +
      result.meet.is + "\n" +
      result.event.is + "\n" +
      "Place: " + result.place.is + "\n" +
      "Seed time: " + result.seedTime.is + "\n" +
      "Final time: " + result.finalTime.is + "\n";

    sendEmail(recipientAddresses, subject, body)
  }

}
