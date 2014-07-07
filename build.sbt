sbtPlugin := true

organization := "net.ground5hark.sbt"

name := "sbt-closure"

version := "0.1.0"

scalaVersion := "2.10.4"

libraryDependencies += "com.google.javascript" % "closure-compiler" % "v20140625"

addSbtPlugin("com.typesafe.sbt" %% "sbt-web" % "1.0.2")

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
