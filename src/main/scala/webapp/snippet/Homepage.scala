package webapp.snippet

import net.liftweb.common.Full
import net.liftweb.util.BindHelpers._
import org.slf4j.LoggerFactory
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
      {swimmer.mostRecentMeetResultsHtml}
    </div>
    </li>
  }

  def listSwimmers = {
    User.currentUser match {
      case Full(user) =>
        "#entry" #> {
          user.allWatchedSwimmers map {
            s =>
              "#swimmerName" #> <h1>
                {s.name.is.fullName}
              </h1> &
                "#results" #> s.mostRecentMeetResultsHtml
          }
        }
      case _ => "*" #> <div>
        <h2>Welcome to Swim Meet Alerts!</h2>
        <a href={User.loginPageURL}>Login</a>
        or
        <a href={User.signUpPath.mkString("/", "/", "")}>sign up</a>
      </div>
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
