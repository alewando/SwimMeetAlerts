package webapp.snippet

import net.liftweb.util.Helpers._
import org.slf4j.LoggerFactory
import net.liftweb.http.{RequestVar, SHtml}
import model.{User, Swimmer}

class Swimmers {

  val log = LoggerFactory.getLogger(getClass())

  def listswimmers = {
    val user = User.currentUser.openTheBox
    "*" #>
      Swimmer.findAll.map {
        swimmer =>
          def processWatch() {
            swimmer.addWatcher(user)
          }
          def clearResults() {
            log.info("Clearing results for swimmer {}", swimmer.name)
            swimmer.results(Nil).save
          }
          val sel = "#name" #> swimmer.name.is.fullName & "#submit" #> SHtml.submit("Watch", processWatch)
          sel & "#clearResults" #> SHtml.submit("Clear Results", clearResults)
      }
  }

  def addswimmer = {
    object firstName extends RequestVar("")
    object lastName extends RequestVar("")

    def processAdd() {
      log.info("Adding swimmer. first={}, last={}", firstName.is, lastName.is)
      Swimmer.createForName(firstName.is, lastName.is)
    }

    "#firstname" #> SHtml.text(firstName.is, firstName(_)) &
      "#lastname" #> SHtml.text(lastName.is, lastName(_)) &
      "#submit" #> SHtml.submit("Add", processAdd)
  }

}
