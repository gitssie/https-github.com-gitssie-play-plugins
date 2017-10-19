import sbt.ClasspathDep

name := """play-plugins"""

version := "1.0"

lazy val DefaultSettings:Seq[Def.SettingsDefinition] = Seq(
    generateReverseRouter := false,
    scalaVersion := "2.12.2"
)

lazy val PlayRoutes = (project in file("src/play-routes")).enablePlugins(PlayScala).settings(DefaultSettings : _*)

lazy val PlayDeadbolt = (project in file("src/play-deadbolt")).enablePlugins(PlayScala).settings(DefaultSettings : _*)

lazy val PlaySpring = (project in file("src/play-spring")).enablePlugins(PlayScala).settings(DefaultSettings : _*)

lazy val dependProjects = Seq[ClasspathDep[ProjectReference]](
    PlayRoutes,
    PlayDeadbolt,
    PlaySpring
)
lazy val publishedProjects = Seq[ProjectReference](
    PlayRoutes,
    PlayDeadbolt,
    PlaySpring
)
lazy val root = (project in file(".")).enablePlugins(PlayScala).settings(DefaultSettings : _*)
    .dependsOn(dependProjects :_*)
    .aggregate(publishedProjects :_*)

//libraryDependencies += guice

// Test Database
//libraryDependencies += "com.h2database" % "h2" % "1.4.194"
//libraryDependencies += "com.kenshoo" %% "metrics-play" % "2.6.2_0.6.1"
//libraryDependencies += "com.digitaltangible" %% "play-guard" % "2.1.0"
//libraryDependencies += "be.objectify" %% "deadbolt-java" % "2.6.1"
//libraryDependencies += "org.springframework" % "spring-context" % "4.3.10.RELEASE"
//libraryDependencies += "com.actimust"% "play-spring-loader" % "1.0.0"

// Testing libraries for dealing with CompletionStage...
//libraryDependencies += "org.assertj" % "assertj-core" % "3.6.2" % Test
//libraryDependencies += "org.awaitility" % "awaitility" % "2.0.0" % Test
// Make verbose tests
testOptions in Test := Seq(Tests.Argument(TestFrameworks.JUnit, "-a", "-v"))
