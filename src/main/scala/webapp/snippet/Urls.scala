package webapp.snippet

import org.slf4j.LoggerFactory
import model.MeetUrl
import net.liftweb.util.Helpers._
import net.liftweb.common.Full
import webapp.WebApp
import actors.ScrapeAllMeets
import net.liftweb.http.{S, RequestVar, SHtml}

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
      var u = newUrl.is
      if (!u.toLowerCase.startsWith("http://")) u = "http://" + u
      // Check for existing record first
      MeetUrl.find(u) match {
        case Full(_) => S.warning("URL already being tracked: " + u)
        case _ => {
          val url: MeetUrl = MeetUrl.createRecord
          url.id(u)
          if (url.validMeetUrl_?) {
            log.info("Adding URL {}", u)
            url.save
          }
          else S.error("Invalid meet URL")
        }
      }
    }
    "#url" #> SHtml.text(newUrl.is, newUrl(_)) &
      "#submit" #> SHtml.submit("Add", doAdd)
  }
}
