package webapp.snippet

import org.slf4j.LoggerFactory
import model.MeetUrl
import net.liftweb.util.Helpers._
import net.liftweb.http.{RequestVar, SHtml}
import net.liftweb.common.Full
import webapp.WebApp
import actors.ScrapeAllMeets

/**
 * Snippets for working with MeetUrl collection
 */
class Urls {

  val log = LoggerFactory.getLogger(this.getClass)

  def listUrls = {
    def doScrape() {
      WebApp.driver ! ScrapeAllMeets
    }

    val trans = "#url" #> MeetUrl.findAll.map {
      url => {
        val u = url.id.is
        def doRemove() {
          url.delete_!
        }
        "a *" #> u & "a [href]" #> u & "#remove" #> SHtml.submit("Remove", doRemove)
      }
    }
    trans & "#scrapeAll" #> SHtml.submit("Scrape All", doScrape)

  }

  def addUrl = {
    object newUrl extends RequestVar[String]("")

    def doAdd() {
      // Check for existing record first
      MeetUrl.find(newUrl.is) match {
        case Full(_) => log.warn("URL already being tracked: {}", newUrl.is)
        case _ => {
          log.info("Adding URL {}", newUrl.is)
          MeetUrl.createRecord.id(newUrl.is).save
        }
      }
    }
    "#url" #> SHtml.text(newUrl.is, newUrl(_)) &
      "#submit" #> SHtml.submit("Add", doAdd)
  }
}
