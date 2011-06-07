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

    val meet = new Meet("http://www.alewando.com/~adam/test_meet", "nkc")
    //val meet = new Meet("http://results.teamunify.com", "nkc")
    println(meet.eventUrl)
    val scraper = new Scraper(meet)
    val x = scraper.events
    for (event <- x; result <- scraper.eventResults(event.id); if result.entrant.fullName.contains("Carman")) {
      println(event);
      println(result);
      val personalResults = db("personResults").findOne(MongoDBObject("id" -> result.entrant.fullName));
      val dbResult = grater[Result].asDBObject(result);


    }

  }
}