name := "SwimMeetAlerts"

scalaVersion := "2.9.1"

libraryDependencies ++= Seq(
    "javax.mail" % "mail" % "1.4.1",
    "com.mongodb.casbah" %% "casbah" % "2.1.5-1",
    "org.slf4j" % "slf4j-api" % "1.6.1" withSources(),
    "org.mortbay.jetty" % "jetty" % "6.1.26" % "test",
    "junit" % "junit" % "4.7" % "test",
    "ch.qos.logback" % "logback-classic" % "0.9.26",
    "org.scala-tools.testing" %% "specs" % "1.6.9" % "test",
    "com.h2database" % "h2" % "1.2.147"
)

{
  val liftVersion = "2.4-M4"
  libraryDependencies ++=  Seq(
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile" withSources(),
    "net.liftweb" %% "lift-mapper" % liftVersion % "compile" withSources()
  )
}

resolvers += "repo.novus rels" at "http://repo.novus.com/snapshots/"

libraryDependencies += "com.novus" %% "salat-core" % "0.0.8-SNAPSHOT" withSources()
