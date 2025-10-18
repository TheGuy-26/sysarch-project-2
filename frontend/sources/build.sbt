name := """webui"""
organization := "saarland.sic.sysarch"

version := "1.1"

lazy val root = (project in file(".")).enablePlugins(PlayScala, LauncherJarPlugin)

scalaVersion := "2.13.13"

libraryDependencies += guice
libraryDependencies += ws
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.0" % Test

Compile / doc / sources := Seq.empty

Compile / packageDoc / publishArtifact := false

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "saarland.sic.sysarch.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "saarland.sic.sysarch.binders._"
