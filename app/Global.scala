import grizzled.slf4j.Logging
import play.api._
import play.api.Play.current
import play.api.libs.concurrent.Akka
import akka.actor.Props
import actors._
import akka.routing.RoundRobinRouter
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._

object Global extends GlobalSettings with Logging {

  override def onStart(app: Application) {
    initActors
    info("Application has started")
  }

  def initActors = {
    Akka.system.actorOf(Props[EmailSender], name = "emailSender")
    Akka.system.actorOf(Props[ResultProcessor].withRouter(RoundRobinRouter(15)), name = "resultProcessor")
    val driver = Akka.system.actorOf(Props[Driver], name = "driver")
    info("Initialized actors")

    // Every 10 minutes (starting immediately), execute the scraper job
    Akka.system.scheduler.schedule(0.seconds, 10.minutes, driver, ScrapeAllMeets)
  }

}