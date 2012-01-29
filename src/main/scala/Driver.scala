import javax.mail.internet.{InternetAddress, MimeMessage}
import javax.mail.{Address, Transport, Message, Session}
import scala.actors.Actor._
import scala.actors.Actor
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import com.mongodb.casbah.Imports._
import org.slf4j.LoggerFactory

object Driver {
  val log = LoggerFactory.getLogger(this.getClass)
  val MAX_WAIT = 60000

  val mongo = MongoConnection("ds029837.mongolab.com", 29837)
  mongo.authenticate("app","apppw")
  val db = mongo("meetResults")

  RegisterJodaTimeConversionHelpers()
  ResultProcessor.start()
  EmailSender.start()

  def main(args: Array[String]) {
    try {
      //val meet = new Meet("http://www.alewando.com/~adam/test_meet", "nkc") \
      //val meet = new Meet("http://swimmakos.com", "realtime")
      //val meet = new Meet("http://results.teamunify.com", "nkc")
      //val meet = new Meet("http://results.teamunify.com", "ohmmr")
      val meet = new Meet("http://results.teamunify.com", "isfast")
      log.info(meet.name + ":" + meet.url)
      Scraper.start()
      Scraper.scrapeMeet(meet)

    } catch {
      case e: Exception => log.error("Error", e);
    }

    var totalWait = 0;
    while (totalWait < MAX_WAIT && Coordinator.hasOutstandingTasks) {
      log.info("Waiting for tasks to complete");
      Thread.sleep(1000);
      totalWait += 1000;
    }
    log.info("All tasks complete. exiting")
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
  val db = MongoConnection()("meetResults")

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

    val coll = db("personResults")
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
      EmailSender ! (result, emailRecips)
      log.info("Adding new event record: " + record)
      coll += record
    }
  }

  def getEmailRecipientsForEntrant(result: Result): List[String] = {
    // Look up email recipients
    val coll = db("personEmail")
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
        case _  => log.error("Unknown message")
      }
    }
  }

  def sendEmail(result: Result, recipientAddresses: List[String]) {
    log.info("Sending email to " + recipientAddresses.length + " recipients")
    // Set up the mail object
    val message = new MimeMessage(session)

    // Set the from, to, subject, body text
    message.setFrom(new InternetAddress("SwimMeetAlerts@alewando.com"))

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

    // And send it
    Transport.send(message)
  }
}
