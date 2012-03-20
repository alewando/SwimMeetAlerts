package webapp

import actors.{Driver, EmailSender, ResultProcessor, EventScraper}
import akka.actor.{Props, ActorSystem}
import org.eclipse.jetty.server.handler.{ContextHandlerCollection, ContextHandler, ResourceHandler}
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.eclipse.jetty.webapp.WebAppContext
import org.slf4j.LoggerFactory
import akka.routing.RoundRobinRouter

object WebApp extends App {

  val log = LoggerFactory.getLogger(this.getClass())

  // Start the actor system
  val actors: ActorSystem = initActors
  val driver = actors.actorFor("/user/driver")

  // Start Jetty container
  val jettyServer = startJetty

  // Start the scheduler
  log.info("Starting scheduler")
  Scheduler.scheduleJobs

  // Join the Jetty server thread
  jettyServer.join()

  def initActors = {
    val system = ActorSystem("AlertsSystem")
    system.actorOf(Props[Driver], name = "driver")
    system.actorOf(Props[ResultProcessor].withRouter(RoundRobinRouter(15)), name = "resultProcessor")
    system.actorOf(Props[EmailSender], name = "emailSender")
    system
  }

  def startJetty : Server = {

    val port = if (System.getenv("PORT") != null) System.getenv("PORT").toInt else 5000
    val server = new Server
    val scc = new SelectChannelConnector
    scc.setPort(port)
    server.setConnectors(Array(scc))

    val webApp = new WebAppContext("src/main/webapp", "/");
    val sampleMeetsHandler = new ContextHandler("/sampleMeets")
    val sampleMeets = new ResourceHandler()
    sampleMeets.setDirectoriesListed(true)
    sampleMeets.setResourceBase("src/test/sampleMeets")
    sampleMeetsHandler.setHandler(sampleMeets)

    val handlers = new ContextHandlerCollection()
    handlers.setHandlers(Array(webApp, sampleMeetsHandler))
    server.setHandler(handlers)

    server.start()
    server
  }

}