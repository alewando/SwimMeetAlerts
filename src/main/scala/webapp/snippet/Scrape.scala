package webapp.snippet

import webapp.{Config, WebApp}
import scala.xml.NodeSeq
import net.liftweb.util._
import Helpers._
import org.slf4j.LoggerFactory
import net.liftweb.http._
import net.liftweb.http.rest.RestHelper
import actors.ScrapeMeet


class Scrape {
  val log = LoggerFactory.getLogger(this.getClass())

  val driver = WebApp.driver

  object meetId extends RequestVar("")

  def scrapeit(xhtml: NodeSeq): NodeSeq = {

    def doScrape() {
      log.info("I should be scraping " + meetId + " now");
      driver ! ScrapeMeet(Config.BASE_URL + "/" + meetId)
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
      WebApp.driver ! ScrapeMeet(Config.BASE_URL + "/" + id)
      <result></result>
    }
  }
}