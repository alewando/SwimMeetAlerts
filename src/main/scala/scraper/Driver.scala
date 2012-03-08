package scraper

import config.Config._
import javax.mail.internet.{InternetAddress, MimeMessage}
import scala.actors.Actor._
import scala.actors.Actor
import com.mongodb.casbah.Imports._
import org.slf4j.LoggerFactory
import java.util.Properties
import javax.mail._

object DB {
  val log = LoggerFactory.getLogger(this.getClass());

  var db: MongoDB = initializeDb

  def uri = {
    val strUri = Option(System.getenv().get("MONGOLAB_URI")) getOrElse {
      log.warn("No MongoDB URI set in MONGOLAB_URI environment variable, using default value of " + DEFAULT_DB_URL)
      DEFAULT_DB_URL
    }
    log.info("Mongo DB URI=" + strUri)
    new com.mongodb.MongoURI(strUri)
  }

  def initializeDb = {
    try {
      db = MongoConnection(uri)(uri.getDatabase)
      if (uri.getUsername() != null && uri.getPassword() != null) {
        db.authenticate(uri.getUsername(), new String(uri.getPassword()))
      }
    } catch {
      case e => log.error("Error connecting to DB: " + e)
    }
    if (db == null) {
      log.error("No DB connection")
    }
    db
  }

  def apply(x: String): MongoCollection = {
    if (db == null) {
      db = initializeDb
    }
    db(x)
  }

  def ping = {
    db.stats
  }
}

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

    val emailRecips = getEmailRecipientsForEntrant(result) match {
      case Nil => return
      case x: List[String] => x
    }

    val coll = DB("personResults")
    val dboPersonalResults = coll.findOne(MongoDBObject("firstName" -> result.entrant.firstName, "lastName" -> result.entrant.lastName,
      "meet" -> result.event.meet.name, "event" -> result.event.name))
    if (dboPersonalResults.isDefined) {
      log.debug("Not replacing existing event record for event " + result.event.name)
    } else {
      val builder = MongoDBObject.newBuilder
      builder += "firstName" -> result.entrant.firstName
      builder += "lastName" -> result.entrant.lastName
      builder += "meet" -> result.event.meet.name
      builder += "event" -> result.event.name
      builder += "age" -> result.age
      builder += "team" -> result.team
      builder += "seedTime" -> result.seedTime
      builder += "finalTime" -> result.finalTime
      val record = builder.result()
      EmailSender !(result, emailRecips)
      log.info("Adding new event record: " + record)
      coll += record
    }
  }

  def getEmailRecipientsForEntrant(result: Result): List[String] = {
    // Look up email recipients
    val coll = DB("personEmail")
    //TODO: Log error if collection doesn't exist (give example doc)

    var res: List[String] = Nil
    coll.findOne(MongoDBObject("name" -> result.entrant.fullName)).foreach {
      x =>
        res = x.as[BasicDBList]("emailRecipients").toList collect {
          case s: String => s
        }
    }

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
