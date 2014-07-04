sbtPlugin := true

organization := "net.ground5hark.sbt"

name := "sbt-closure"

version := "1.0.0"

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % "1.0.2"
)

resolvers ++= Seq(Resolver.mavenLocal)

publishMavenStyle := false

scriptedSettings

scriptedLaunchOpts ++= Seq(
  "-Xmx1024M",
  "-XX:MaxPermSize=256M",
  s"-Dproject.version=${version.value}"
)
