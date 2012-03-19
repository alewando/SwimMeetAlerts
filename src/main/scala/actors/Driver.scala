package actors

import webapp.Config._
import javax.mail.internet.{InternetAddress, MimeMessage}
import org.slf4j.LoggerFactory
import java.util.Properties
import javax.mail._
import net.liftweb.mongodb.BsonDSL._
import model.{User, Result, Swimmer}
import akka.actor.{Props, Actor}
import akka.routing.RoundRobinRouter

//TODO: Make Driver an actor
class Driver extends Actor {
  val log = LoggerFactory.getLogger(this.getClass)
  val MAX_WAIT = 60000

  val EventLink = """^<a href="(.+).htm" target=main>([^<]*)</a>.*""".r;

  val eventScraper = context.actorOf(Props[EventScraper].withRouter(RoundRobinRouter(15)), name = "eventScraper")

  def receive = {
    case meet: Meet => scrapeMeet(meet)
  }

  // TODO: Create a MeetScraper actor to do this
  def scrapeMeet(meet: Meet) = {
    // Scrape events from meet page
    for (event <- events(meet)) {
      // Send each event as a message to the actors
      log.trace("Sending message for event: {}", event.id)
      eventScraper ! event
    }
    log.debug("Done scraping event list for meet: {}", meet.name)
  }

  /**
   * Scrape events for a specific meet
   */
  def events(meet: Meet): List[Event] = {
    var lEvents = List[Event]()
    for (line <- meet.eventsPage.getLines(); m <- EventLink findAllIn line) m match {
      case EventLink(id, name) =>
        val eventUrl = meet.url + "/" + id + ".htm"
        lEvents = new Event(id, meet, name.trim, eventUrl) :: lEvents
    }
    lEvents.reverse
  }
}

/**
 * Processes a scraped scrapedResult record. Saves new results to DB and sends an email.
 */
class ResultProcessor extends Actor {
  val log = LoggerFactory.getLogger(this.getClass)

  val emailSender = context.actorFor("../emailSender")

  def receive = {
    case result: ScrapedResult =>
      handleResult(result)
  }

  def handleResult(scrapedResult: ScrapedResult) {
    log.trace("{}", scrapedResult);

    // See if this scrapedResult is for a swimmer being watched
    val swimmer = Swimmer.findForResult(scrapedResult) openOr {
      return
    }
    log.debug("Result for tracked swimmer: " + scrapedResult)

    // Look for existing scrapedResult in DB
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
      emailSender !(result, emailRecips)
    }
  }

  def getEmailRecipientsForSwimmer(swimmer: Swimmer): List[String] = {
    for (watcher <- swimmer.watchers.value;
         u <- User.find(watcher)
    ) yield {
      u.email.value
    }
  }
}

class EmailSender extends Actor {
  val log = LoggerFactory.getLogger(this.getClass)

  val props = new Properties();
  props.put("mail.smtp.host", SMTP_SERVER);
  if (SMTP_USER != null) {
    props.put("mail.smtp.user", SMTP_USER);
  }
  val session = Session.getInstance(props, null);

  def receive = {
    case (result: Result, recipients: List[String]) =>
      try {
        sendEmail(result, recipients)
      } catch {
        // TODO: Let actor's parent handle the error
        case e => log.error("Error sending email", e)
      }
  }

  def sendEmail(result: Result, recipientAddresses: List[String]) {
    log.info("Sending email to {} for result {}", recipientAddresses, result)
    // Set up the mail object
    val message = new MimeMessage(session)

    // Set the from, to, subject, body text
    message.setFrom(new InternetAddress(EMAIL_FROM_ADDRESS))

    val recipients: Array[Address] = recipientAddresses.map(new InternetAddress(_)).toArray

    // TODO: Send separate email (or use BCC?)
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
