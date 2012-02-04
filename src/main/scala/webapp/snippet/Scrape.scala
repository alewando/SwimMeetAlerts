package webapp.snippet

import scala.xml.{ NodeSeq, Text }
import net.liftweb.util._
import net.liftweb.common._
import Helpers._
import org.slf4j.LoggerFactory
import net.liftweb.http._
import scraper.Driver
import net.liftweb.http.rest.RestHelper


class Scrape {
	val log = LoggerFactory.getLogger(this.getClass())
	
	object meetId extends RequestVar("")
	
	def scrapeit(xhtml: NodeSeq) : NodeSeq = {
	  
	  def doScrape() {
	    log.info("I should be scraping "+meetId+" now");
	    Driver.scrapeMeet(meetId)
	  }
	  
	  bind("meet", xhtml,
	      "meetid" -> SHtml.text(meetId, meetId(_)),
	      "submit" -> SHtml.submit("Scrape", doScrape))
	}

}

object Scrape extends RestHelper {
  	val log = LoggerFactory.getLogger(this.getClass())

  	// REST handler
	serve {
	  case "scrape" :: id :: _ XmlGet _=> { 
	    log.info("REST: scrape "+id)
	    Driver.scrapeMeet(id)
	    <result></result>
	  }
	}
}