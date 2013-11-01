import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "swimmeetalerts"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    "jp.t2v" %% "play2.auth"      % "0.9",
    "org.mindrot" % "jbcrypt" % "0.3m",
    "se.radley" %% "play-plugins-salat" % "1.3.0",
    "net.databinder" %% "dispatch-http" % "0.8.9" excludeAll (
      ExclusionRule(organization = "commons-logging")),
    "net.databinder" %% "dispatch-jsoup" % "0.8.9",
    "joda-time" % "joda-time" % "2.1",
    "org.joda" % "joda-convert" % "1.2",
    "org.jsoup" % "jsoup" % "1.6.3",
    "javax.mail" % "mail" % "1.4.1",
    "org.mongodb" % "mongo-java-driver" % "2.7.3",
    "org.slf4j" % "slf4j-api" % "1.6.1",
    "org.slf4j" % "jcl-over-slf4j" % "1.6.1",
    "com.typesafe" %% "scalalogging-slf4j" % "1.0.1",
    "ch.qos.logback" % "logback-classic" % "0.9.26",
    "org.scala-tools.testing" %% "specs" % "1.6.9" % "test",
    "junit" % "junit" % "4.7" % "test")

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
    routesImport += "se.radley.plugin.salat.Binders._",
    templatesImport += "org.bson.types.ObjectId",
    resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")
    .settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
}
