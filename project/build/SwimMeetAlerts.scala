import sbt._

class SwimMeetAlerts(info: ProjectInfo) extends DefaultProject(info){
  val casbah = "com.mongodb.casbah" %% "casbah" % "2.1.5.0"
  val novusRels = "repo.novus rels" at "http://repo.novus.com/releases/"
  val salat = "com.novus" %% "salat-core" % "0.0.7"
}