import play.GlobalSettings
import grizzled.slf4j.Logging

object Global extends GlobalSettings with Logging {
 override def onStart(app: Application) {
    info("Application has started")
  }  
}