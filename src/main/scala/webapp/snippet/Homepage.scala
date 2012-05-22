package webapp.snippet

import net.liftweb.common.Full
import net.liftweb.util.BindHelpers._
import org.slf4j.LoggerFactory
import net.liftweb.http.{SHtml, RequestVar}
import model.{MeetUrl, Swimmer, User}
import xml.Text

/**
 * Snippet for the homepage
 */
class Homepage {
  val log = LoggerFactory.getLogger(this.getClass)

  def swimmerHtml(swimmer: Swimmer) = {
    <li>
      {swimmer.name.is.fullName}<div class="results">
      {swimmer.recentEventsHtml}
    </div>
    </li>
  }

  def listSwimmers = {
    User.currentUser match {
      case Full(user) =>
        "#entry" #> {
          user.allWatchedSwimmers map {
            s =>
              "#swimmerName" #> s.name.is.fullName &
                "#results" #> s.recentEventsHtml
          }
        }
      case _ => "*" #> <div>
        <a href={User.loginPageURL}>Login</a>
        or
        <a href={User.signUpPath.mkString("/", "/", "")}>sign up</a>
      </div>
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

      // TODO: Get the swimmer list to reflect the new swimmer (redirect to home page?)
    }

    User.currentUser match {
      case Full(user) =>
        "#firstName" #> SHtml.text(firstName.is, firstName(_)) &
          "#lastName" #> SHtml.text(lastName.is, lastName(_)) &
          "#submit" #> SHtml.submit("Add", processAddSwimmer)
      case _ => "*" #> ""
    }
  }

  def inProgress = {
    MeetUrl.inProgressMeets match {
      case Nil =>
        "#meet" #> Text("None active")
      case meets =>
        "#meet" #> meets.map {
          url => {
            val u = url.id.is
            "a *" #> url.name & "a [href]" #> u
          }
        }
    }
  }

  def addMeet = {
    User.currentUser match {
      case Full(_) => "#link [href]" #> "addMeet"
      case _ => "*" #> ""
    }
  }
}
