package webapp.snippet


class Dump {

  case class ResultRecord(firstName: String, lastName: String, meet: String, event: String, age: Integer, team: String, seedTime: String, finalTime: String)

  //  def dumpresults(xhtml: NodeSeq): NodeSeq = {
  //    val recs = for {dbo <- DB("personResults")} yield
  //      new ResultRecord(
  //        dbo.as[String]("firstName"), dbo.as[String]("lastName"), dbo.as[String]("meet"), dbo.as[String]("event"), dbo.as[Integer]("age"), dbo.as[String]("team"), dbo.as[String]("seedTime"), dbo.as[String]("finalTime"))
  //
  //    def bindResults(template: NodeSeq): NodeSeq = {
  //      recs.flatMap{
  //        case ResultRecord(firstName, lastName, meet, event, age, team, seedTime, finalTime) =>
  //          bind("scrapedResult", template, "name" -> firstName, "meet" -> meet, "event" -> event, "finaltime" -> finalTime)
  //      } toSeq
  //    }
  //    bind("resultList", xhtml, "results" -> bindResults _)
  //  }
}
