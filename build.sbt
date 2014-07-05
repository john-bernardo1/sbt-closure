sbtPlugin := true

organization := "net.ground5hark.sbt"

name := "sbt-closure"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.10.4"

libraryDependencies <++= (sbtVersion) {
  sv => Seq(
    "com.google.javascript" % "closure-compiler" % "v20140625",
    // TODO Potentially binary incompatible
    Defaults.sbtPluginExtra("com.typesafe.sbt" %% "sbt-web" % "1.0.2", "0.13", "2.10")
  )
}

resolvers ++= Seq(
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
  Resolver.url("sbt snapshot plugins", url("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots"))(Resolver.ivyStylePatterns),
  Resolver.sonatypeRepo("snapshots"),
  "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/snapshots/",
  Resolver.mavenLocal
)

publishMavenStyle := false

scriptedSettings

scriptedLaunchOpts ++= Seq(
  "-Xmx1024M",
  "-XX:MaxPermSize=256M",
  s"-Dproject.version=${version.value}"
)
