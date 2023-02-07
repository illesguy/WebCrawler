organization := "com.illesguy"

name := "WebCrawler"

version := "1.0"

scalaVersion := "2.13.1"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
  "org.jsoup" % "jsoup" % "1.11.3",
  "org.slf4j" % "slf4j-simple" % "1.6.1",
  "junit" % "junit" % "4.12",
  "org.mockito" % "mockito-all" % "1.8.4",
  "org.scalatest" %% "scalatest" % "3.0.8"
)

enablePlugins(UniversalPlugin)

val qualityCheck = taskKey[Unit]("Run tests and checks on project")

val build = taskKey[Unit]("Build project")
build := Def.sequential(
  clean in Compile,
  compile in Compile,
  test in Test
).value
