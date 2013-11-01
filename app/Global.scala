import scala.concurrent.duration.DurationInt

import com.typesafe.scalalogging.slf4j.Logging

import actors.Driver
import actors.EmailSender
import actors.ResultProcessor
import actors.ScrapeAllMeets
import akka.actor.Props
import akka.routing.RoundRobinRouter
import play.api.Application
import play.api.GlobalSettings
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import actors._

object Global extends GlobalSettings with Logging {

  override def onStart(app: Application) {
    initActors
    logger.info("Application has started")
  }

  def initActors = {
    Akka.system.actorOf(Props[EmailSender], name = "emailSender")
    Akka.system.actorOf(Props[ResultProcessor].withRouter(RoundRobinRouter(15)), name = "resultProcessor")
    val driver = Akka.system.actorOf(Props[Driver], name = "driver")
    logger.info("Initialized actors")

    // Every 10 minutes (starting immediately), execute the scraper job
    Akka.system.scheduler.schedule(0.seconds, 10.minutes, driver, ScrapeAllMeets)
  }

}