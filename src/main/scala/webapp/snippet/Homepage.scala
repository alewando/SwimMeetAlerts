package webapp.snippet

import net.liftweb.common.Full
import net.liftweb.util.BindHelpers._
import org.slf4j.LoggerFactory
import model.{Swimmer, User}
import net.liftweb.http.{SHtml, RequestVar}

/**
 * Snippet for the homepage
 */
class Homepage {
  val log = LoggerFactory.getLogger(this.getClass)

  def listSwimmers = {
    User.currentUser match {
      case Full(user) =>
        "#entry" #> {
          user.allWatchedSwimmers map {
            s =>
              def processRemove() = {
                s.removeWatcher(user)
              }

              "#name" #> s.name.get.fullName & "#remove" #> SHtml.submit("Remove", processRemove)
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
