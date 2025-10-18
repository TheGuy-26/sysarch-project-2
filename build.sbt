import java.nio.file.{Files, Paths, StandardCopyOption}
import java.text.SimpleDateFormat
import java.util.Date
import sbt.IO

lazy val createSubmission = taskKey[Unit]("Create a submission zip file")

createSubmission := {
  val baseDirectoryValue = baseDirectory.value
  val srcDirectory = baseDirectoryValue / "programs"
  val targetDirectory = baseDirectoryValue / "submissions"
  val contributionsFile = baseDirectoryValue / "CONTRIBUTIONS.md"
  val timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())
  val zipFileName = s"submission_$timestamp.zip"

  // Create the submissions directory
  IO.createDirectory(targetDirectory)

  // Zip the main folder
  val zipFile = targetDirectory / zipFileName
  val allFiles = (srcDirectory ** "*").get
  val mappings = allFiles.map(file =>
    (file, baseDirectoryValue.toPath.relativize(file.toPath).toString)
  ) :+ (contributionsFile, baseDirectoryValue.toPath.relativize(contributionsFile.toPath).toString)
  IO.zip(mappings, zipFile, None)

  println(s"Submission created: ${zipFile.getPath}")
}

