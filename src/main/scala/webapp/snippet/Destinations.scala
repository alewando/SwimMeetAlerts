package webapp.snippet

import org.slf4j.LoggerFactory
import model.User

import net.liftweb.util.Helpers._
import net.liftweb.common.Full
import net.liftweb.http.{SHtml, RequestVar, S}

class Destinations {
  val log = LoggerFactory.getLogger(this.getClass)


  def listDestinations = {
    User.currentUser match {
      case Full(user) => {
        "#userEmail" #> user.email &
          "#addressDisplay" #> {
            user.extraDestinations.is.map {
              dest: String =>
                "#address" #> <span>
                  {dest}
                </span>
            }
          }
      }
      case _ => "*" #> {
        S.error("Not logged in")
        <div></div>
      }
    }
  }

  def addDestination = {
    object emailToAdd extends RequestVar("")

    def processAddDestination() {
      val user = User.currentUser.openTheBox
      log.info("Adding email address {} to user {}", emailToAdd.is, user.niceName)

      // Add the current user as a watcher
      user.addDestination(emailToAdd)

      // Clear variables
      emailToAdd("")
    }

    User.currentUser match {
      case Full(user) =>
        "#emailToAdd" #> SHtml.text(emailToAdd.is, emailToAdd(_)) &
          "#submit" #> SHtml.submit("Add", processAddDestination)
      case _ => "*" #> ""
    }
  }


}
