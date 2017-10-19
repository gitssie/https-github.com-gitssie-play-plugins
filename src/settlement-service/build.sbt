name := """settlement-service"""

version := "1.0"

scalaVersion := "2.12.2"

//libraryDependencies += cacheApi


libraryDependencies ++= Seq(
  "org.iq80.leveldb" % "leveldb" % "0.9"
)