import com.mongodb._
import casbah.commons.conversions.scala._
import com.mongodb.casbah.Imports._
import javax.mail.internet.{InternetAddress, MimeMessage}
import javax.mail.{Transport, Message, Session}

object Driver {
  RegisterJodaTimeConversionHelpers()

  def main(args: Array[String]) {
    val mongo = MongoConnection();
    val db = mongo("meetResults")
    val coll = db("personResults")

    //val meet = new Meet("http://www.alewando.com/~adam/test_meet", "nkc")
    val meet = new Meet("http://results.teamunify.com", "nkc")
    println(meet.name + ":" + meet.url)
    val scraper = new Scraper(meet)
    val x = scraper.events
    // TODO: Better filtering (ie: not hard-coded name)
    for (event <- x; result <- scraper.eventResults(event.id); if result.entrant.fullName.contains("Carman")) {
      println(event);
      println(result);
      val dboPersonalResults = coll.findOne(MongoDBObject("firstName" -> result.entrant.firstName, "lastName" -> result.entrant.lastName,
        "meet" -> meet.name, "event" -> event.name))
      if (dboPersonalResults.isDefined) {
        println("Not replacing existing event record for event " + event.name)
      } else {
        val builder = MongoDBObject.newBuilder
        builder += "firstName" -> result.entrant.firstName
        builder += "lastName" -> result.entrant.lastName
        builder += "meet" -> meet.name
        builder += "event" -> event.name
        builder += "age" -> result.age
        builder += "team" -> result.team
        builder += "seedTime" -> result.seedTime
        builder += "finalTime" -> result.finalTime
        val record = builder.result()
        sendEmail(record)
        println("Adding new event record")
        coll += record
      }
    }

  }

  def sendEmail(record: MongoDBObject) {
    // Set up the mail object
    val properties = System.getProperties
    properties.put("mail.smtp.host", "kobe.alewando.com")
    val session = Session.getDefaultInstance(properties)
    val message = new MimeMessage(session)

    // Set the from, to, subject, body text
    message.setFrom(new InternetAddress("SwimMeetAlerts@alewando.com"))
    message.setRecipients(Message.RecipientType.TO, "adam@alewando.com")
    message.setSubject("New result for " + record("firstName") + " " + record("lastName") + ": " + record("event"))
    val body = record("firstName") + " " + record("lastName") + "\n" +
      record("meet") + "\n" +
      record("event") + "\n" +
      "Seed time: " + record("seedTime") + "\n" +
      "Final time: " + record("finalTime") + "\n";
    message.setText(body)

    // And send it
    Transport.send(message)
  }
}

case class PersonResults(person: Person, results: List[Result])
