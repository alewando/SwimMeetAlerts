import org.mortbay.jetty.Connector
import org.mortbay.jetty.Server
import org.mortbay.jetty.webapp.WebAppContext
import org.mortbay.jetty.nio._
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.core.util.StatusPrinter
import org.slf4j.LoggerFactory
import net.liftweb.http.LiftRules

object RunWebApp extends Application {
  
      // assume SLF4J is bound to logback in the current environment
    val lc : LoggerContext = LoggerFactory.getILoggerFactory().asInstanceOf[LoggerContext]
    // print logback's internal status
    StatusPrinter.print(lc);
  
  val server = new Server
  val scc = new SelectChannelConnector
  scc.setPort(8080)
  server.setConnectors(Array(scc))

  val context = new WebAppContext()
  context.setServer(server)
  context.setContextPath("/")
  context.setWar("src/main/webapp")

  server.addHandler(context)
  server.start()
}
