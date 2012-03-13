package webapp.snippet

import xml.NodeSeq
import net.liftweb.util.Helpers._
import org.slf4j.LoggerFactory
import net.liftweb.mongodb.BsonDSL._
import net.liftweb.http.{RequestVar, SHtml}
import model.{Name, User, Swimmer}

class Swimmers {

  val log = LoggerFactory.getLogger(getClass())

  def testform(xhtml: NodeSeq): NodeSeq = {

    def processSubmit() {
      log.error("!!!! processSubmit clicked")
    }

    log.info("testform snippet")
    bind("test", xhtml, "submit" -> SHtml.submit("Submit", processSubmit))
  }

  def listswimmers(xhtml: NodeSeq): NodeSeq = {
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

  def addswimmer(xhtml: NodeSeq): NodeSeq = {
    object firstName extends RequestVar("")
    object lastName extends RequestVar("")

    def processAdd() {
      log.info("Adding swimmer. first={}, last={}", firstName.is, lastName.is)
      Swimmer.createRecord.name(Name.createRecord.firstName(firstName.is).lastName(lastName.is)).save
    }

    bind("swimmer", xhtml,
      "firstname" -> SHtml.text(firstName.is, firstName(_)),
      "lastname" -> SHtml.text(lastName.is, lastName(_)),
      "submit" -> SHtml.submit("Add", processAdd))
  }

}
