ThisBuild / scalaVersion := "2.13.16"
ThisBuild / version := "0.1.0"
ThisBuild / organization := "SysArch"

val chiselVersion = "6.7.0"
val scalaVersionMajor = "2.13"

import java.nio.file.{Files, Paths, StandardCopyOption}
import java.text.SimpleDateFormat
import java.util.Date
import sbt.IO

import java.nio.file._
import scala.collection.JavaConverters._



lazy val root = (project in file("."))
  .settings(
    name := "RISC-V",
    // autoAPIMappings := true,
    apiMappings ++= {
      val cp: Seq[Attributed[File]] = (Compile / fullClasspath).value
      def findManagedDependency(organization: String, name: String): File = {
        (for {
          entry <- cp
          module <- entry.get(moduleID.key)
          if module.organization == organization
          if module.name.startsWith(name)
          jarFile = entry.data
        } yield jarFile).head
      }
      Map(
        findManagedDependency("org.chipsalliance", "chisel") -> url(
          s"https://javadoc.io/doc/org.chipsalliance/chisel_$scalaVersionMajor/$chiselVersion"
        )
      )
    },
    libraryDependencies ++= Seq(
      "org.chipsalliance" %% "chisel" % chiselVersion,
      "org.scalatest" %% "scalatest" % "3.2.19" % "test",
      "edu.berkeley.cs" %% "chiseltest" % "6.0.0" % "test",
      "io.circe" %% "circe-core" % "0.14.13",
      "io.circe" %% "circe-generic" % "0.14.13",
      "io.circe" %% "circe-parser" % "0.14.13",
      "com.softwaremill.sttp.client4" %% "core" % "4.0.3",
      "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1"
    ),
    scalacOptions ++= Seq(
      "-language:reflectiveCalls",
      "-deprecation",
      "-feature",
      "-Xcheckinit",
      "-Ymacro-annotations"
    ),
    addCompilerPlugin(
      "org.chipsalliance" % "chisel-plugin" % chiselVersion cross CrossVersion.full
    )
  )

   // Define the task key for copying the jars and dependencies
val copyDependencies = taskKey[Unit]("Copy JAR, test JAR, and all (test) dependencies to a single folder")

copyDependencies := {
  val log = streams.value.log

  // Define the output directory
  val outputDir = target.value / "dependency-jars"
  IO.createDirectory(outputDir)

  // Helper function to copy a file if it is not a directory
  def copyFileIfNotDirectory(source: File, targetDir: File): Unit = {
    if (source.isFile) {
      val targetFile = targetDir / source.getName
      IO.copyFile(source, targetFile)
      log.info(s"Copied ${source.getName} to: $targetFile")
    }
  }

  // Copy main JAR
  val mainJar = (Compile / packageBin).value
  copyFileIfNotDirectory(mainJar, outputDir)

  // Copy test JAR
  val testJar = (Test / packageBin).value
  copyFileIfNotDirectory(testJar, outputDir)

  // Copy all compile dependencies
  val compileDeps = (Compile / dependencyClasspath).value.map(_.data)
  compileDeps.foreach(file => copyFileIfNotDirectory(file, outputDir))

  // Copy all test dependencies
  val testDeps = (Test / dependencyClasspath).value.map(_.data)
  testDeps.foreach(file => copyFileIfNotDirectory(file, outputDir))

  log.info(s"All dependencies copied to: $outputDir")
}


lazy val listClasses = taskKey[List[String]]("List all classes")

listClasses := {
  val targetDirectory = (Compile / classDirectory).value
  val classFiles = Files
    .walk(targetDirectory.toPath)
    .iterator()
    .asScala
    .filter(path => path.toString.endsWith(".class"))
    .toList

  classFiles.map { path =>
    targetDirectory.toPath
      .relativize(path)
      .iterator()
      .asScala
      .map(_.toString())
      .mkString(".")
      .stripSuffix(".class")
  }
}