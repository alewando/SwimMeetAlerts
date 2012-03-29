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
        sendEmail(swimmer, result, recipients)
  }

  def sendEmail(swimmer: Swimmer, result: Result, recipientAddresses: List[String]) {
    log.info("Sending email to {} for result {}", recipientAddresses, result)
    // Set up the mail object
    val message = new MimeMessage(session)

    // Set the from, to, subject, body text
    message.setFrom(new InternetAddress(EMAIL_FROM_ADDRESS))

    val recipients: Array[Address] = recipientAddresses.map(new InternetAddress(_)).toArray

    message.setRecipients(Message.RecipientType.BCC, recipients)
    message.setSubject("New Result for " + swimmer.name.value.fullName + ": " + result.event.is)
    val body = swimmer.name.value.fullName + "\n" +
      result.meet.is + "\n" +
      result.event.is + "\n" +
      "Place: " + result.place.is + "\n" +
      "Seed time: " + result.seedTime.is + "\n" +
      "Final time: " + result.finalTime.is + "\n";
    message.setText(body)

    try {
      sendMessage(message)
    } catch {
      case e =>
        log.error("send failed, exception: " + e);
    }
  }

  def sendMessage(msg: Message) = {
    val trans = session.getTransport("smtp")
    trans.connect(SMTP_SERVER, 25, SMTP_USER, SMTP_PASSWORD)
    trans.sendMessage(msg, msg.getAllRecipients())
    trans.close()
  }
}
