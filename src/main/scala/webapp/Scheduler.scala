package webapp

import org.quartz.impl.StdSchedulerFactory
import org.quartz._
import org.slf4j.LoggerFactory
import org.joda.time.DateTime
import model.MeetUrl
import cc.spray.client.HttpConduit
import java.net.URL
import cc.spray.http._
import cc.spray.http.HttpMethods._
import akka.dispatch.Future
import java.text.SimpleDateFormat
import java.util.Date
import actors.ScrapeMeet

object Scheduler {
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
  val driver = WebApp.driver

  override def execute(ctx: JobExecutionContext) {
    log.debug("Running meet scraper job")

    // Check all of the registered meet URLs
    val twoWeeksAgo = DateTime.now.minusWeeks(2).toDate
    for (url <- MeetUrl.findAll; lastMod <- getLastModified(url)) {
      if (url.inProgress.value) {
        // Mark as complete if we've been scraping for 2 weeks without completion
        if (lastMod.compareTo(twoWeeksAgo) < 0) {
          log.warn("Meet {} has been in progress for two weeks, marking as complete", url.id.is)
          url.inProgress(false).lastCompleted(new Date()).save
        } else {
          // Otherwise, scrape the meet for latest results
          log.debug("Scraping in-progress meet: {}", url.id.is)
          driver ! ScrapeMeet(url)
        }
      } else if (lastMod.compareTo(url.lastCompleted.is) > 0) {
        // Completed date is older than the last modified date, this URL is active again
        log.info("Meet {} has become active", url.id.is)
        url.inProgress(true).save
        log.debug("Scraping newly active meet: {}", url.id.is)
        driver ! ScrapeMeet(url)
      }
    }
  }

  def getLastModified(url: MeetUrl): Future[Date] = {
    val dateParser = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z")
    val u = new URL(url.id.is)
    val conduit = new HttpConduit(u.getHost())
    val headResp = conduit.sendReceive(HttpRequest(HEAD, "/evtindex.htm"))
    headResp map {
      resp => resp.headers.find(_.name equals "Last-Modified") map {
        x =>
          dateParser.parse(x.value)
      } getOrElse {
        log.warn("URL {} has no Last-Modified header, using current date")
        new Date()
      }
    }
  }
}

