package webapp.snippet

import net.liftweb.util.Helpers._
import org.slf4j.LoggerFactory
import model.{User, Swimmer}
import net.liftweb.common.Full
import net.liftweb.http.{S, RequestVar, SHtml}

class Swimmers {

  val log = LoggerFactory.getLogger(getClass())

  def mySwimmers = {
    User.currentUser match {
      case Full(user) =>
        "#swimmer" #> {
          user.allWatchedSwimmers map {
            s =>
              "#swimmerName" #> <h1>
                {s.name.is.fullName}
              </h1> & "#swimmerResults" #>
                s.allResultsHtml
            //TODO: Button to unfollow
          }
        }
      case _ => "*" #> {
        S.error("Not logged in")
        <div></div>
      }
    }
  }

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

        // TODO: Add ability to add/remove an alias for the swimmer
      }
  }

  def addswimmer = {
    object firstName extends RequestVar("")
    object lastName extends RequestVar("")

    def processAddSwimmer() {
      log.info("Adding swimmer. first={}, last={}", firstName.is, lastName.is)
      // Look for an existing swimmer, or add a new one
      val swimmer = Swimmer.findForName(firstName.is + " " + lastName.is) match {
        case Full(swimmer) => swimmer
        case _ => Swimmer.createForName(firstName.is, lastName.is)
      }

      // Add the current user as a watcher
      swimmer.addWatcher(User.currentUser.get)

      // Clear variables
      firstName("")
      lastName("")
    }

    User.currentUser match {
      case Full(user) =>
        "#firstName" #> SHtml.text(firstName.is, firstName(_)) &
          "#lastName" #> SHtml.text(lastName.is, lastName(_)) &
          "#submit" #> SHtml.submit("Add", processAddSwimmer)
      case _ => "*" #> ""
    }
  }

}
