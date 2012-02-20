package scraper

import javax.mail.internet.{InternetAddress, MimeMessage}
import scala.actors.Actor._
import scala.actors.Actor
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import com.mongodb.casbah.Imports._
import org.slf4j.LoggerFactory
import java.util.Properties
import javax.mail._

object DB {
  val log = LoggerFactory.getLogger(this.getClass());

  var db: MongoDB = initializeDb

  val DEFAULT_DB_URL = "mongodb://kobe:27017/meetResults"

  def initializeDb = {
    val strUri = Option(System.getenv().get("MONGOLAB_URI")) getOrElse DEFAULT_DB_URL

    if (strUri == null) {
      throw new RuntimeException("No MongoDB URI set in MONGOLAB_URI environment variable")
    }
    log.info("Mongo DB URI=" + strUri)
    val uri = new com.mongodb.MongoURI(strUri)

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

  RegisterJodaTimeConversionHelpers()
  ResultProcessor.start()
  EmailSender.start()
  Scraper.start()

  //val DEFAULT_MEET_ID = "isfast";
  //val DEFAULT_MEET_ID = "nkc";
  val DEFAULT_MEET_ID = "ohmmr"

  def main(args: Array[String]) {

    scrapeMeet(DEFAULT_MEET_ID)

    var totalWait = 0;
    while (totalWait < MAX_WAIT && Coordinator.hasOutstandingTasks) {
      log.info("Waiting for tasks to complete");
      Thread.sleep(1000);
      totalWait += 1000;
    }
    log.info("All tasks complete. exiting")
  }

  def scrapeMeet(meetId: String) {
    try {
      //val meet = new Meet("http://www.alewando.com/~adam/test_meet", "nkc") \
      //val meet = new Meet("http://swimmakos.com", "realtime")
      val meet = new Meet("http://results.teamunify.com", meetId)
      log.info("Scraping " + meet.name + ": " + meet.url)
      Scraper.scrapeMeet(meet)

    } catch {
      case e: Exception => log.error("Error", e);
    }
  }
}

/**
 * Tracks outstanding (still executing) tasks
 */
object Coordinator {
  var outstandingTasks = 0;
  var taskCount = 0;

  def taskStarted {
    synchronized {
      taskCount += 1;
      outstandingTasks += 1;
    }
  }

  def taskFinished {
    synchronized {
      outstandingTasks -= 1
    }
  }

  def hasOutstandingTasks: Boolean = synchronized {
    outstandingTasks > 0
  };
}

case object Stop

/**
 * Processes a scraped result record. Saves new results to DB and sends an email.
 */
object ResultProcessor extends Actor {
  val log = LoggerFactory.getLogger(this.getClass)

  def act() {
    loop {
      react {
        case result: Result => actor {
          try {
            Coordinator.taskStarted
            handleResult(result)
          } finally {
            Coordinator.taskFinished
          }
        }
        case Stop => {
          EmailSender ! Stop
          exit()
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

  val SENDGRID_SMTP_SERVER = "smtp.sendgrid.net"

  val log = LoggerFactory.getLogger(this.getClass)

  val session = {
    val properties = System.getProperties
    properties.put("mail.smtp.host", "192.168.0.1")
    Session.getDefaultInstance(properties)
  }

  def act() {
    loop {
      react {
        case (result: Result, recipients: List[String]) =>
          Coordinator.taskStarted
          try {
            // TODO: Iterate over recipients, send separate email (or use BCC?)
            sendEmail(result, recipients)
          } catch {
            case e => log.error("Error sending email", e)
          } finally {
            Coordinator.taskFinished
          }
        case Stop => exit()
        case _ => log.error("Unknown message")
      }
    }
  }

  def sendEmail(result: Result, recipientAddresses: List[String]) {
    log.info("Sending email to " + recipientAddresses.length + " recipients")
    // Set up the mail object
    val message = new MimeMessage(session)

    // Set the from, to, subject, body text
    message.setFrom(new InternetAddress("alert@swimmeetalerts.com"))

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
      case MessagingException =>
        log.error("send failed, exception: " + _);
    }
  }

  def sendMessage(msg: Message) = {
    Properties props = new Properties();
    val smtpUser = System.getenv("SENDGRID_USERNAME")
    val smtpPassword = System.getenv("SENDGRID_PASSWORD")
    val smtpServer = if (smtpUser == null) "localhost" else SENDGRID_SMTP_SERVER

    props.put("mail.smtp.host", smtpServer);
    props.put("mail.smtp.user", smtpUser);
    props.put("mail.from", "alert@swimmeetalerts.com");
    val sess = Session.getInstance(props, null);
    val trans = sess.getTransport("smtps")
    trans.connect(smtpServer, 25, smtpUser, smtpPassword)
    trans.sendMessage(msg, msg.getAllRecipients())
    trans.close()

  }
}
