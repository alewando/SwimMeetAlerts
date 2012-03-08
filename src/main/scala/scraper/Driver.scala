package scraper

import config.Config._
import javax.mail.internet.{InternetAddress, MimeMessage}
import scala.actors.Actor._
import scala.actors.Actor
import com.mongodb.casbah.Imports._
import org.slf4j.LoggerFactory
import java.util.Properties
import javax.mail._
import model.Swimmer

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
 * Processes a scraped result record. Saves new results to DB and sends an email.
 */
object ResultProcessor extends Actor {
  val log = LoggerFactory.getLogger(this.getClass)

  def act() {
    loop {
      react {
        case result: Result => actor {
          handleResult(result)
        }
      }
    }
  }

  def handleResult(result: Result) {
    log.debug(result.toString);

    // See if this result is for a swimmer being watched
    val swimmer = Swimmer.findForResult(result)
    swimmer match {
      case null => {
        log.debug("No match on result for "+result.entrant)
        return
      }
    }
    log.debug("!!!!!!!!!!!!!!!!!!!! FOUND RESULT FOR TRACKED SWIMMER: "+result)

    // TODO: Look for existing result in DB
    val exists = false
    if (exists) {
      log.debug("Not replacing existing event record for event " + result.event.name)
    } else {
      // TODO: Create and save new result record
//      val builder = MongoDBObject.newBuilder
//      builder += "firstName" -> result.entrant.firstName
//      builder += "lastName" -> result.entrant.lastName
//      builder += "meet" -> result.event.meet.name
//      builder += "event" -> result.event.name
//      builder += "age" -> result.age
//      builder += "team" -> result.team
//      builder += "seedTime" -> result.seedTime
//      builder += "finalTime" -> result.finalTime
//      val record = builder.result()
//      log.info("Adding new event record: " + record)
//      coll += record

      // Get emails for swimmer's watchers
      val emailRecips = getEmailRecipientsForSwimmer(swimmer) match {
        case Nil => return
        case x: List[String] => x
      }
      EmailSender !(result, emailRecips)
    }
  }

  def getEmailRecipientsForSwimmer(result: Result): List[String] = {
    // TODO: Look up email recipients (swimmer -> watchers -*> email)
    //val coll = DB("personEmail")

    var res: List[String] = Nil
//    coll.findOne(MongoDBObject("name" -> result.entrant.fullName)).foreach {
//      x =>
//        res = x.as[BasicDBList]("emailRecipients").toList collect {
//          case s: String => s
//        }
//    }

    res
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
    message.setSubject("New result for " + result.entrant.fullName + ": " + result.event.name)
    val body = result.entrant.fullName + "\n" +
      result.event.meet.name + "\n" +
      result.event.name + "\n" +
      "Place: " + result.place + "\n" +
      "Seed time: " + result.seedTime + "\n" +
      "Final time: " + result.finalTime + "\n";
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
