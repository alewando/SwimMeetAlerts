import com.typesafe.startscript.StartScriptPlugin

seq(StartScriptPlugin.startScriptForClassesSettings: _*)

seq(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)

name := "SwimMeetAlerts"

scalaVersion := "2.9.1"

resolvers += "Java.net Maven2 Repository" at "http://download.java.net/maven/2/"

resolvers += "Scala Tools Snapshots" at "http://scala-tools.org/repo-snapshots"

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases"

libraryDependencies ++= {
  val liftVersion = "2.4"
  Seq(
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default",
    "net.liftweb" %% "lift-mongodb-record" % liftVersion % "compile->default"
  )
}

libraryDependencies ++= Seq(
    "com.typesafe.akka" % "akka-actor" % "2.0",
    "com.typesafe.akka" % "akka-slf4j" % "2.0",
    "net.databinder" %% "dispatch-http" % "0.8.8" excludeAll(
        ExclusionRule(organization="commons-logging")
    ),
    "net.databinder" %% "dispatch-jsoup" % "0.8.8",
    "joda-time" % "joda-time" % "2.1",
    "org.joda" % "joda-convert" % "1.2",
    "org.jsoup" % "jsoup" % "1.6.3",
    "javax.mail" % "mail" % "1.4.1",
    "org.mongodb" % "mongo-java-driver" % "2.7.3",
    "org.slf4j" % "slf4j-api" % "1.6.1",
    "org.slf4j" % "jcl-over-slf4j" % "1.6.1",
    "ch.qos.logback" % "logback-classic" % "0.9.26",
    "org.scala-tools.testing" %% "specs" % "1.6.9" % "test",
    "junit" % "junit" % "4.7" % "test"
)

libraryDependencies ++= Seq(
    "org.eclipse.jetty" % "jetty-server" % "7.3.1.v20110307" % "compile->default",
    "org.eclipse.jetty" % "jetty-servlet" % "7.3.1.v20110307" % "compile->default",
    "org.eclipse.jetty" % "jetty-webapp" % "7.3.1.v20110307" % "compile->default"
)

