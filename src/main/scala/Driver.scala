import com.novus.salat._
import com.novus.salat.global._
import com.mongodb._
import casbah.commons.conversions.scala._
import com.mongodb.casbah.Imports._

object Driver {
  RegisterJodaTimeConversionHelpers()

  def main(args: Array[String]) {
    val mongo = MongoConnection();
    val db = mongo("meetResults")
    val coll = db("personResults")

    val meet = new Meet("http://www.alewando.com/~adam/test_meet", "nkc")
    //val meet = new Meet("http://results.teamunify.com", "nkc")
    println(meet.eventUrl)
    val scraper = new Scraper(meet)
    val x = scraper.events
    for (event <- x; result <- scraper.eventResults(event.id); if result.entrant.fullName.contains("Carman")) {
      println(event);
      println(result);
      val dboPersonalResults = coll.findOne(MongoDBObject("firstName" -> result.entrant.firstName, "lastName" -> result.entrant.lastName))
      if (dboPersonalResults isDefined) {
        val personResults = grater[PersonResults].asObject(dboPersonalResults.get)
        //personResults.results.
      } else {
        var personResults = new PersonResults(result.entrant, List[Result](result))
        coll += grater[PersonResults].asDBObject(personResults)
      }
      //val personResults = if (dboPersonalResults.isDefined) grater[PersonResults].asObject(dboPersonalResults) else new PersonResults(result.entrant, List[Result]())
      //personResults.results += result

    }

  }
}

case class PersonResults(person: Person, results: List[Result])