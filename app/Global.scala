import play.api.GlobalSettings
import grizzled.slf4j.Logging
import play.api.Application

object Global extends GlobalSettings with Logging {

  override def onStart(app: Application) {
    info("Application has started")
  }

}