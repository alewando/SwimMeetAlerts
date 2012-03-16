package webapp.snippet

import net.liftweb.common.Full
import xml.{Text, NodeSeq}
import net.liftweb.util.Helpers._
import model.{Swimmer, User}
import net.liftweb.http.{RequestVar, SHtml}
import org.slf4j.LoggerFactory

/**
 * Snippet for the homepage
 */
class Homepage {
  val log = LoggerFactory.getLogger(this.getClass)

  def listSwimmers(xhtml: NodeSeq): NodeSeq = User.currentUser match {
    case Full(user) => {
      val swimmers = user.allWatchedSwimmers match {
        case Nil => Text("Not following any swimmers, add one below")
        case swimmers => swimmers.flatMap {
          swimmer =>
            bind("swimmer", chooseTemplate("swimmer", "entry", xhtml),
              "name" -> Text(swimmer.name.is.fullName)
            )
        }
      }
      bind("swimmer", xhtml, "entry" -> swimmers)
    }
    case _ => <div>
      <a href={User.loginPageURL}>Login</a>
      or
      <a href={User.signUpPath.mkString("/", "/", "")}>sign up</a>
    </div>
  }

  def addswimmer(xhtml: NodeSeq): NodeSeq = {
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
        bind("swimmer", xhtml,
          "firstname" -> SHtml.text(firstName.is, firstName(_)),
          "lastname" -> SHtml.text(lastName.is, lastName(_)),
          "submit" -> SHtml.submit("Add", processAddSwimmer))
      case _ => <div></div>
    }
  }
}
