package webapp.snippet

import xml.NodeSeq
import net.liftweb.util.Helpers._
import org.slf4j.LoggerFactory
import net.liftweb.http.{RequestVar, SHtml}
import model.{Name, User, Swimmer}

class Swimmers {

  val log = LoggerFactory.getLogger(getClass())

  def listswimmers(xhtml: NodeSeq): NodeSeq = {
    Swimmer.findAll.flatMap {
      swimmer => bindSwimmer(swimmer, xhtml)
    }
  }

  def bindSwimmer(swimmer: Swimmer, xhtml: NodeSeq): NodeSeq = {

    def processWatch() {
      val currentUser = User.currentUser.openTheBox
      swimmer.addWatcher(currentUser)
    }

    bind("swimmer", xhtml, "name" -> swimmer.name.is.fullName, "submit" -> SHtml.submit("Watch", processWatch))
  }

  def addswimmer(xhtml: NodeSeq): NodeSeq = {
    object firstName extends RequestVar("")
    object lastName extends RequestVar("")

    def processAdd() {
      log.info("Adding swimmer. first={}, last={}", firstName.is, lastName.is)
      Swimmer.createForName(firstName.is, lastName.is)
    }

    bind("swimmer", xhtml,
      "firstname" -> SHtml.text(firstName.is, firstName(_)),
      "lastname" -> SHtml.text(lastName.is, lastName(_)),
      "submit" -> SHtml.submit("Add", processAdd))
  }

}
