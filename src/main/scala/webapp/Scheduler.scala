package webapp

import org.quartz.impl.StdSchedulerFactory
import org.quartz._
import org.slf4j.LoggerFactory
import model.MeetUrl
import org.joda.time.LocalDate

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

  def getLastModified(url: String): LocalDate = {

  }
}


object MeetToBeScraped
