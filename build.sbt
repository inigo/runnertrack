name := """runnertrack"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
   "org.seleniumhq.selenium" % "selenium-java" % "2.45.0",
   "net.sf.ehcache" % "ehcache" % "2.10.0",
   "org.apache.commons" % "commons-math3" % "3.5"
)
