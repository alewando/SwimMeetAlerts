package scraper

import config.Config._
import javax.mail.internet.{InternetAddress, MimeMessage}
import scala.actors.Actor._
import scala.actors.Actor
import org.slf4j.LoggerFactory
import java.util.Properties
import javax.mail._
import net.liftweb.mongodb.BsonDSL._
import model.{User, Result, Swimmer}
import org.bson.types.ObjectId

//TODO: Make Driver an actor
object Driver {
  val log = LoggerFactory.getLogger(this.getClass)
  val MAX_WAIT = 60000


  def scrapeMeet(meetId: String) {
    try {
      //val meet = new Meet("http://www.alewando.com/~adam/test_meet", "nkc") \
      //val meet = new Meet("http://swimmakos.com", "realtime")
      val meet = new Meet(BASE_URL, meetId)
      log.info("Scraping " + meet.name + ": " + meet.url)
      Scraper.scrapeMeet(meet)

    } catch {
      case e: Exception => log.error("Error", e);
    }
  }
}

/**
 * Processes a scraped scrapedResult record. Saves new results to DB and sends an email.
 */
object ResultProcessor extends Actor {
  val log = LoggerFactory.getLogger(this.getClass)

  def act() {
    loop {
      react {
        case result: ScrapedResult => actor {
          handleResult(result)
        }
      }
    }
  }

  def handleResult(scrapedResult: ScrapedResult) {
    log.trace("{}", scrapedResult);

    // See if this scrapedResult is for a swimmer being watched
    val swimmer = Swimmer.findForResult(scrapedResult) openOr {
      return
    }
    log.debug("Result for tracked swimmer: " + scrapedResult)

    // TODO: Look for existing scrapedResult in DB
    val existing = Result.find(("swimmer" -> swimmer.id.is) ~ ("event" -> scrapedResult.event.name) ~ ("meet" -> scrapedResult.event.meet.name))
    existing openOr {
      // Create and save new scrapedResult record
      log.info("Saving new scrapedResult for tracked swimmer: {}", scrapedResult)
      val result = model.Result.createRecord.swimmer(swimmer.id.is).meet(scrapedResult.event.meet.name).event(scrapedResult.event.name).age(scrapedResult.age).team(scrapedResult.team).seedTime(scrapedResult.seedTime).finalTime(scrapedResult.finalTime).save

      // Get emails for swimmer's watchers
      val emailRecips = getEmailRecipientsForSwimmer(swimmer) match {
        case Nil => return
        case x: List[String] => x
      }
      EmailSender !(result, emailRecips)
    }
  }

  def getEmailRecipientsForSwimmer(swimmer: Swimmer): List[String] = {
    // TODO: Look up email recipients (swimmer -> watchers -*> email)
//    val emails = for {watcherId: ObjectId <- swimmer.watchers.value}
//    yield User.find(watcherId) map {
//      _.email.is
//    }
//    log.info("Emails for swimmer {}: {}", swimmer.name.value.fullName, emails)
//    emails
      Nil
  }
}

object EmailSender extends Actor {
  val log = LoggerFactory.getLogger(this.getClass)

  val props = new Properties();
  props.put("mail.smtp.host", SMTP_SERVER);
  if (SMTP_USER != null) {
    props.put("mail.smtp.user", SMTP_USER);
  }
  val session = Session.getInstance(props, null);

  def act() {
    loop {
      react {
        case (result: Result, recipients: List[String]) =>
          try {
            // TODO: Iterate over recipients, send separate email (or use BCC?)
            sendEmail(result, recipients)
          } catch {
            case e => log.error("Error sending email", e)
          }
        case _ => log.error("Unknown message")
      }
    }
  }

  def sendEmail(result: Result, recipientAddresses: List[String]) {
    log.info("Sending email to " + recipientAddresses.length + " recipients")
    // Set up the mail object
    val message = new MimeMessage(session)

    // Set the from, to, subject, body text
    message.setFrom(new InternetAddress(EMAIL_FROM_ADDRESS))

    val recipients: Array[Address] = recipientAddresses.map(new InternetAddress(_)).toArray

    message.setRecipients(Message.RecipientType.TO, recipients)
    val swimmer = result.swimmer.obj.openTheBox
    message.setSubject("New scrapedResult for " + swimmer + ": " + result.event.name)
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
