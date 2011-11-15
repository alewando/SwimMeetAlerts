package webapp.snippet

import scala.xml.{ NodeSeq, Text }
import net.liftweb.util._
import net.liftweb.common._
import java.util.Date
import Helpers._

class HelloWorld {

  def howdy = { "#stuff *" #> "TESTING" }
  
  def render = "#stuff *" #> "test"

  /*
   lazy val date: Date = DependencyFactory.time.vend // create the date via factory

   def howdy = "#time *" #> date.toString
   */
}

