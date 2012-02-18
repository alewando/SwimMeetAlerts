import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{DefaultServlet, ServletContextHandler}
import org.eclipse.jetty.server.nio.SelectChannelConnector
import net.liftweb.http.LiftFilter
import org.eclipse.jetty.webapp.WebAppContext

object JettyLauncher extends Application {
  
  if(System.getenv().get("MONGOLAB_URI")==null) {
    //System.getenv().put("MONGOLAB_URI","mongodb://heroku_app2660393:c3btjte2421prcbe8qlfi7nn9u@ds029837.mongolab.com:29837/heroku_app2660393")
  }
  
  val port = if(System.getenv("PORT") != null) System.getenv("PORT").toInt else 5000
  val server = new Server
  val scc = new SelectChannelConnector
  scc.setPort(port)
  server.setConnectors(Array(scc))

  //val context = new ServletContextHandler(server, "/", ServletContextHandler.NO_SESSIONS)
  //val context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS)
  val context = new WebAppContext(server,"src/main/webapp","/");
  context.setContextPath("/")
  //context.addServlet(classOf[DefaultServlet], "/")
  //context.addFilter(classOf[LiftFilter], "/*", 0).setInitParameter("bootloader","lift.LiftBootstrap")
  //context.setResourceBase("src/main/webapp")

  server.start
  server.join
}