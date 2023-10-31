version := "0.1.0-SNAPSHOT"
scalaVersion := "2.13.12"
maintainer := "sleefd@gmail.com"

lazy val root = (project in file("."))
  .settings(
    name := "json2struct",
    organization := "me.yceel"
  )

libraryDependencies ++= Seq(
  "org.json4s" %% "json4s-native" % "4.0.6",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "2.3.0",
  "org.scalacheck" %% "scalacheck" % "1.15.4",
  "org.scalatest" %% "scalatest" % "3.2.15" % "test",
  "org.rogach" %% "scallop" % "5.0.0"
)

enablePlugins(JavaAppPackaging, UniversalPlugin)
Compile / mainClass := Some("json2struct.cli.Cli")
Universal / mappings ++= Seq(file("README.md") -> "README.md")

publishTo := Some("Github repo" at "https://maven.pkg.github.com/" + System.getenv("GITHUB_REPOSITORY"))
publishMavenStyle := true
credentials += Credentials(
  "GitHub Package Registry",
  "maven.pkg.github.com",
  System.getenv("GITHUB_REPOSITORY_OWNER"),
  System.getenv("GITHUB_TOKEN")
)

Test / packageDoc / publishArtifact := false
Test / packageSrc / publishArtifact := false
Test / packageBin / publishArtifact := false
Compile / packageDoc / publishArtifact := false
