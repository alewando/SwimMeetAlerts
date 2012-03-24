import com.typesafe.startscript.StartScriptPlugin

seq(StartScriptPlugin.startScriptForClassesSettings: _*)

name := "SwimMeetAlerts"

scalaVersion := "2.9.1"

resolvers += "Java.net Maven2 Repository" at "http://download.java.net/maven/2/"

resolvers += "Scala Tools Snapshots" at "http://scala-tools.org/repo-snapshots"

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases"

//resolvers += "Spray http lib Repo" at "http://repo.spray.cc/"

//resolvers += "repo.novus rels" at "http://repo.novus.com/snapshots/"

libraryDependencies ++= {
  val liftVersion = "2.4-M4" 
  Seq(
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default" withSources(),
    "net.liftweb" %% "lift-mapper" % liftVersion % "compile->default" withSources(),
    "net.liftweb" %% "lift-mongodb" % liftVersion % "compile->default" withSources(),
    "net.liftweb" %% "lift-mongodb-record" % liftVersion % "compile->default" withSources()
  )
}

libraryDependencies ++= Seq(
    "com.typesafe.akka" % "akka-actor" % "2.0",
    "com.typesafe.akka" % "akka-slf4j" % "2.0",
    "net.databinder" %% "dispatch-http" % "0.8.8",
    "joda-time" % "joda-time" % "2.1",
    "org.joda" % "joda-convert" % "1.2",
    "javax.mail" % "mail" % "1.4.1",
    "org.quartz-scheduler" % "quartz" % "2.1.3",
    "org.mongodb" % "mongo-java-driver" % "2.7.3",
    "org.slf4j" % "slf4j-api" % "1.6.1" withSources(),
    "ch.qos.logback" % "logback-classic" % "0.9.26",
    "org.scala-tools.testing" %% "specs" % "1.6.9" % "test",
    "junit" % "junit" % "4.7" % "test"
)

libraryDependencies ++= Seq(
    "org.eclipse.jetty" % "jetty-server" % "7.3.1.v20110307" % "compile->default",
    "org.eclipse.jetty" % "jetty-servlet" % "7.3.1.v20110307" % "compile->default",
    "org.eclipse.jetty" % "jetty-webapp" % "7.3.1.v20110307" % "compile->default"
)

