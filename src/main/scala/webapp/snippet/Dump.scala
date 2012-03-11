package webapp.snippet

import xml.NodeSeq
import net.liftweb.util.Helpers._
import org.slf4j.LoggerFactory
import net.liftweb.http.SHtml
import model.{User, Swimmer}
import net.liftweb.mongodb.BsonDSL._

class Dump {

  val log = LoggerFactory.getLogger(getClass())

  def testform(xhtml: NodeSeq): NodeSeq = {

    def processSubmit() {
      log.error("!!!! processSubmit clicked")
    }

    log.info("testform snippet")
    bind("test", xhtml, "submit" -> SHtml.submit("Submit", processSubmit))
  }

  def dumpswimmers(xhtml: NodeSeq): NodeSeq = {

    Swimmer.findAll.flatMap {
      swimmer => bindSwimmer(swimmer, xhtml)
    }
  }

  def bindSwimmer(swimmer: Swimmer, xhtml: NodeSeq): NodeSeq = {

    def processWatch() {
      log.warn("Processing submission. swimmer: {}", swimmer)
      val select = ("_id" -> swimmer.id.is)
      val update = ("$addToSet" ->("watchers", User.currentUser.openTheBox.id))
      log.warn("select: {}, update: {}", select, update)
      Swimmer.update(select, update)
    }

    bind("swimmer", xhtml, "name" -> swimmer.name.is.fullName, "submit" -> SHtml.submit("Watch", processWatch))
  }

}
