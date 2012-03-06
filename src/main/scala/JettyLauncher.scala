import org.eclipse.jetty.server.handler.{ContextHandlerCollection, ContextHandler, ResourceHandler, HandlerList}
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{DefaultServlet, ServletContextHandler}
import org.eclipse.jetty.server.nio.SelectChannelConnector
import net.liftweb.http.LiftFilter
import org.eclipse.jetty.webapp.WebAppContext

object JettyLauncher extends App {
  
  if(System.getenv().get("MONGOLAB_URI")==null) {
    //System.getenv().put("MONGOLAB_URI","mongodb://heroku_app2660393:c3btjte2421prcbe8qlfi7nn9u@ds029837.mongolab.com:29837/heroku_app2660393")
  }
  
  val port = if(System.getenv("PORT") != null) System.getenv("PORT").toInt else 5000
  val server = new Server
  val scc = new SelectChannelConnector
  scc.setPort(port)
  server.setConnectors(Array(scc))

  val webApp = new WebAppContext("src/main/webapp","/");
  //context.setContextPath("/")

  val sampleMeetsHandler = new ContextHandler("/sampleMeets")
  val sampleMeets = new ResourceHandler()
  sampleMeets.setDirectoriesListed(true)
  sampleMeets.setResourceBase("src/test/sampleMeets")
  sampleMeetsHandler.setHandler(sampleMeets)

  val handlers = new ContextHandlerCollection()
  handlers.setHandlers(Array(webApp, sampleMeetsHandler))
  server.setHandler(handlers)

  server.start
  server.join
}