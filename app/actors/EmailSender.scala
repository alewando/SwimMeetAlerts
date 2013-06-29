package actors

import app.Config._
import javax.mail.internet.{ InternetAddress, MimeMessage }
import org.slf4j.LoggerFactory
import java.util.Properties
import javax.mail._
import akka.actor.Actor
import models.{ Swimmer, EventResult }
import grizzled.slf4j.Logging

class EmailSender extends Actor with Logging {

  val props = new Properties();
  props.put("mail.smtp.host", SMTP_SERVER);
  if (SMTP_USER != null) {
    props.put("mail.smtp.user", SMTP_USER);
  }
  val session = Session.getInstance(props, null);

  def receive = {
    case (swimmer: Swimmer, result: EventResult, recipients: List[String]) =>
      sendResultEmail(swimmer, result, recipients)
    case AdminMessage(subject, message) => {
      info("Sending admin email w/ subject '" + subject + "'")
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
        error("send failed, exception: " + e);
    }

  }

  def sendResultEmail(swimmer: Swimmer, result: EventResult, recipientAddresses: List[String]) {
    info("Sending email to {} for result {}", recipientAddresses, result)

    // TODO: Calculate delta between seed and final times (use joda Period to parse, convert to Duration for calc)

    val subject = "New Result for " + swimmer.fullName + ": " + result.event
    val body = swimmer.fullName + "\n" +
      result.meet + "\n" +
      result.event + "\n" +
      "Place: " + result.place + "\n" +
      "Seed time: " + result.seedTime + "\n" +
      "Final time: " + result.finalTime + "\n" + {
        result.delta map {
          "Change: " + _ + "\n"
        }
      };

    sendEmail(recipientAddresses, subject, body)
  }

}
