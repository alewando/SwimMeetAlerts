package actors

import org.slf4j.LoggerFactory
import akka.actor.{Props, Actor}
import akka.routing.RoundRobinRouter

class Driver extends Actor {
  val log = LoggerFactory.getLogger(this.getClass)
  val MAX_WAIT = 60000

  val meetScraper = context.actorOf(Props[MeetScraper].withRouter(RoundRobinRouter(5)), name = "eventScraper")

  def receive = {
    case meetReq: ScrapeMeet => meetScraper forward meetReq
  }

}
