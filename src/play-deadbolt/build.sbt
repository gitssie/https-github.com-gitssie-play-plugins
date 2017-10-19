name := """play-deadbolt"""

version := "1.0"

scalaVersion := "2.12.2"

libraryDependencies += cacheApi


libraryDependencies ++= Seq(
  "be.objectify" %% "deadbolt-scala" % "2.6.0"
)