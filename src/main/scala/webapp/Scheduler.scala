package webapp

import org.quartz.impl.StdSchedulerFactory
import org.quartz._
import org.slf4j.LoggerFactory
import model.MeetUrl
import org.joda.time.LocalDate
import cc.spray.client.HttpConduit
import java.net.URL
import cc.spray.http._
import cc.spray.http.HttpMethods._
import org.joda.time.format.DateTimeFormat
import akka.dispatch.Future

object Scheduler {
  val driver = WebApp.driver
  val scheduler = StdSchedulerFactory.getDefaultScheduler();
  scheduler.start();

  def scheduleJobs {
    val job = JobBuilder.newJob(classOf[ScraperJob])
      .withIdentity("scraperJob", "group1")
      .build();


    val trigger = TriggerBuilder.newTrigger()
      .withIdentity("scrapeTrigger", "group1")
      .withSchedule(SimpleScheduleBuilder.simpleSchedule()
      .withIntervalInMinutes(10)
      .repeatForever())
      .build()

    // Tell quartz to schedule the job using our trigger
    scheduler.scheduleJob(job, trigger);
  }
}

class ScraperJob extends Job {
  val log = LoggerFactory.getLogger(this.getClass)

  val dateParser = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss Z")

  override def execute(ctx: JobExecutionContext) {
    log.debug("Running meet scraper job")
    //WebApp.driver ! ScrapeMeet(Config.BASE_URL+"/"+Config.DEFAULT_MEET_ID)
    val cutoffDate = new LocalDate().minusWeeks(2)
    val meetsToScrape = MeetUrl.findAll map {
      url =>
        val lastMod = getLastModified(url.id.value)
        if (url.completed.value) {

        }

    }
  }

  def getLastModified(url: String): Future[LocalDate] = {
    val u = new URL(url)
    val conduit = new HttpConduit(u.getHost())
    val headResp = conduit.sendReceive(HttpRequest(HEAD, "/evtindex.htm"))
    headResp map {
      _.headers.find(_.name equals "Last-Modified").map {x =>
        dateParser.parseDateTime(x.value)
      } getOrElse {
        log.warn("URL {} has no Last-Modified header, using current date")
        DateTime.now
      }
    }
  }
}


object MeetToBeScraped
