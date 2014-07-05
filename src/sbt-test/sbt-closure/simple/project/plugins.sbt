libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.6.4"

addSbtPlugin("com.typesafe.sbt" %% "sbt-web" % "1.0.2", "0.13", "2.10")

addSbtPlugin("com.typesafe.sbt" %% "sbt-coffeescript" % "1.0.0")

addSbtPlugin("net.ground5hark.sbt" %% "sbt-closure" % sys.props("project.version"))
