import com.typesafe.sbt.packager.Keys.dockerRepository
import com.typesafe.sbt.packager.docker.Cmd
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.Docker
import sbt.*
import sbt.Keys.*

import scala.io.Source
import scala.util.Using

object Build {
  val noPublish = Seq(
    publish / skip := true,
    publishLocal := {},
    publishArtifact := false
  )

  val githubCredential = Credentials(
    "GitHub Package Registry",
    "maven.pkg.github.com",
    System.getenv("GITHUB_REPOSITORY_OWNER"),
    System.getenv("GITHUB_TOKEN")
  )

  val publishSettings = Seq(
    publishTo := Some("Github repo" at "https://maven.pkg.github.com/" + System.getenv("GITHUB_REPOSITORY")),
    publishMavenStyle := true,
    credentials += githubCredential,
    Test / packageDoc / publishArtifact := false,
    Test / packageSrc / publishArtifact := false,
    Test / packageBin / publishArtifact := false,
    Compile / packageDoc / publishArtifact := false
  )

  val AkkaHttpVersion = "10.6.0"

  val commonDockerSettings = Seq(
    Docker / dockerRepository := Some("ghcr.io/reminia")
  )
  val typesafeConfig = "com.typesafe" % "config" % "1.4.3"

  def dockerfile(file: String): Seq[Cmd] = {
    Using.resource(Source.fromFile(file)) { src =>
      src.getLines().filter(_.nonEmpty).map {
        line =>
          val splits = line.split("\\s+")
          Cmd(splits.head, splits.tail.mkString(" "))
      }.toList
    }
  }
}
