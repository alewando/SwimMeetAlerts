import sbt._

class SwimMeetAlerts(info: ProjectInfo) extends DefaultProject(info){
  val javamail = "javax.mail" % "mail" % "1.4.1"
  val casbah = "com.mongodb.casbah" %% "casbah" % "2.1.5.0"
  val novusRels = "repo.novus rels" at "http://repo.novus.com/releases/"
  val salat = "com.novus" %% "salat-core" % "0.0.7"
  val logger = "org.slf4j" % "slf4j-log4j12" % "1.6.1"
}