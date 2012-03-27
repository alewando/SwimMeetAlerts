//libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-web-plugin" % (v+"-0.2.10"))

resolvers += Classpaths.typesafeResolver

addSbtPlugin("com.typesafe.startscript" % "xsbt-start-script-plugin" % "0.5.0")

//resolvers += "Web plugin repo" at "http://siasia.github.com/maven2"

//addSbtPlugin("com.github.siasia" % "xsbt-web-plugin" % "0.1.2")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.5.2")
