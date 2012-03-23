package webapp.snippet

import webapp.{Config, WebApp}
import scala.xml.NodeSeq
import net.liftweb.util._
import Helpers._
import org.slf4j.LoggerFactory
import net.liftweb.http._
import net.liftweb.http.rest.RestHelper
import actors.ScrapeMeet
import model.MeetUrl


object Scrape {
  val log = LoggerFactory.getLogger(this.getClass())

  val driver = WebApp.driver

  object meetId extends RequestVar[String]("")
  
  def getFullUrl(in: String) = {
    val UrlPattern =  "(?i)http".r
    meetId match {
      case UrlPattern  => meetId.is
      case _ => Config.BASE_URL + "/" + meetId.is
    }    
  }

  def scrapeit(xhtml: NodeSeq): NodeSeq = {

    def doScrape() {
      val url = getFullUrl(meetId.is)
      log.info("I should be scraping " + url + " now");
      val meetUrl : MeetUrl = MeetUrl.createRecord
      meetUrl.id(url)
      driver ! ScrapeMeet(meetUrl)
    }

    bind("meet", xhtml,
      "meetid" -> SHtml.text(meetId, meetId(_)),
      "submit" -> SHtml.submit("Scrape", doScrape))
  }
}

object ScrapeRestHandler extends RestHelper {
  val log = LoggerFactory.getLogger(this.getClass())

  // REST handler
  serve {
    case "scrape" :: id :: _ XmlGet _ => {
      log.info("REST: scrape " + id)
      val url = MeetUrl.createRecord
      url.id(Scrape.getFullUrl(id))
      WebApp.driver ! ScrapeMeet(url)
      <result></result>
    }
  }
}