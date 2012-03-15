package webapp.snippet

import net.liftweb.common.Full
import model.User
import xml.{Text, NodeSeq}
import net.liftweb.util.Helpers._

/**
 * Snippet for the homepage
 */
class Homepage {
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
}
