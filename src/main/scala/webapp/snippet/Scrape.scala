package webapp.snippet

import net.liftweb.util._
import Helpers._
import org.slf4j.LoggerFactory
import net.liftweb.http._
import scraper.Driver
import net.liftweb.http.rest.RestHelper


class Scrape {
  val log = LoggerFactory.getLogger(this.getClass())

  object meetId extends RequestVar("")

  def scrapeIt = {
    "#meetId" #> SHtml.text(meetId, meetId(_)) &
      "#submit" #> SHtml.submit("Scrape", {
        () => Driver.scrapeMeet(meetId)
      })
  }

}

object Scrape extends RestHelper {
  val log = LoggerFactory.getLogger(this.getClass())

  // REST handler
  serve {
    case "scrape" :: id :: _ XmlGet _ => {
      log.info("REST: scrape " + id)
      Driver.scrapeMeet(id)
      <result></result>
    }
  }
}