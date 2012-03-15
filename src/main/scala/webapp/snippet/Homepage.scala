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
      user.allWatchedSwimmers match {
        case Nil => Text("Not following any swimmers, add one below")
        case swimmers => swimmers.flatMap {
          swimmer =>
            bind("swimmer", chooseTemplate("swimmer", "entry", xhtml),
              "name" -> Text(swimmer.name.is.fullName)
            )
        }
      }

    }
    case _ => <lift:embed what="addSwimmer"/>

  }
}
