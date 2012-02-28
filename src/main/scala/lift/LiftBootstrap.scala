package lift

import net.liftweb._
import common._
import http._
import mongodb.{DefaultMongoIdentifier, MongoDB}
import sitemap._
import org.slf4j.LoggerFactory
import webapp.snippet.Scrape
import scraper.{DB, Scheduler}
import com.mongodb.Mongo
import webapp.model.User


/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class LiftBootstrap extends Bootable {
  val log = LoggerFactory.getLogger(this.getClass())

  def boot {
    log.info("Configuring Lift")

    // where to search snippet
    LiftRules.addToPackages("webapp")

    def sitemap(): SiteMap = SiteMap(Menu.i("Home") / "index", Menu.i("Scrape") / "scrape", Menu.i("Dump") / "dump" >> User.AddUserMenusAfter)

    def sitemapMutators = User.sitemapMutator

    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    LiftRules.setSiteMapFunc(() => sitemapMutators(sitemap))

    // What is the function to test if a user is logged in?
    LiftRules.loggedInTest = Full(() => User.loggedIn_?)

    // Use jQuery 1.4
    LiftRules.jsArtifacts = net.liftweb.http.js.jquery.JQuery14Artifacts

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))

    //Don't use state with Heroku
    //LiftRules.autoIncludeAjax = _ => false
    //LiftRules.statelessTest.append {
    //  case _ => true
    //}

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // Add REST dispatches
    LiftRules.statelessDispatchTable.append(Scrape)

    // Configure the database
    configDb

    // Start the scheduler
    Scheduler.scheduleJobs

    // Ping the DB
    DB.ping

  }


  def configDb {
    val uri = DB.uri
    if (uri.getUsername != null) {
      MongoDB.defineDbAuth(DefaultMongoIdentifier, new Mongo(uri), uri.getDatabase, uri.getUsername, new String(uri.getPassword))
    } else {
      MongoDB.defineDb(DefaultMongoIdentifier, new Mongo(uri), uri.getDatabase)
    }
  }

}
