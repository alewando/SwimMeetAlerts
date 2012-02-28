import com.typesafe.startscript.StartScriptPlugin

seq(StartScriptPlugin.startScriptForClassesSettings: _*)

name := "SwimMeetAlerts"

scalaVersion := "2.9.1"


libraryDependencies ++= {
  val liftVersion = "2.4-M4" // Put the current/latest lift version here
  Seq(
    "org.eclipse.jetty" % "jetty-server" % "7.3.1.v20110307" % "compile->default",
    "org.eclipse.jetty" % "jetty-servlet" % "7.3.1.v20110307" % "compile->default",
    "org.eclipse.jetty" % "jetty-webapp" % "7.3.1.v20110307" % "compile->default",
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default" withSources(),
    "net.liftweb" %% "lift-mapper" % liftVersion % "compile->default" withSources(),
    "net.liftweb" %% "lift-mongodb" % liftVersion % "compile->default" withSources(),
    "net.liftweb" %% "lift-mongodb-record" % liftVersion % "compile->default" withSources()
  )
}

libraryDependencies ++= Seq(
    "javax.mail" % "mail" % "1.4.1",
    "org.quartz-scheduler" % "quartz" % "2.1.3",
    "com.mongodb.casbah" %% "casbah" % "2.1.5-1",
    "org.mongodb" % "mongo-java-driver" % "2.7.3",
    "org.slf4j" % "slf4j-api" % "1.6.1" withSources(),
    "org.mortbay.jetty" % "jetty" % "6.1.26" % "test",
    "junit" % "junit" % "4.7" % "test",
    "ch.qos.logback" % "logback-classic" % "0.9.26",
    "org.scala-tools.testing" %% "specs" % "1.6.9" % "test"
)

resolvers += "Java.net Maven2 Repository" at "http://download.java.net/maven/2/"

resolvers += "Scala Tools Snapshots" at "http://scala-tools.org/repo-snapshots"

// resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots"

resolvers += "repo.novus rels" at "http://repo.novus.com/snapshots/"

libraryDependencies += "com.novus" %% "salat-core" % "0.0.8-SNAPSHOT" withSources()
