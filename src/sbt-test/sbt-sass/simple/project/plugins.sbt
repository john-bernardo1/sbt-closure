// Plugins use a different specified scalaVersion from the primary build.sbt
scalaVersion := "2.11.1"

// Plugins still using 2.10.x
addSbtPlugin("com.typesafe.sbt" %% "sbt-web" % "1.0.2", sbtVersion.value, "2.10.4")

addSbtPlugin("net.ground5hark.sbt" %% "sbt-closure" % sys.props("project.version"))
