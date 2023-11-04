import sbt.Keys.{publish, publishArtifact, publishLocal, skip}

object Build {
  val noPublish = Seq(
    publish / skip := true,
    publishLocal := {},
    publishArtifact := false
  )
}
