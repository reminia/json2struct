import Build.{noPublish, publishSettings}

name := "json2struct"

val currVersion = "0.3.0-SNAPSHOT"
val scala2version = "2.13.12"

val commonSettings = Seq(
  version := currVersion,
  scalaVersion := scala2version,
  maintainer := "sleefd@gmail.com",
  organization := "me.yceel.json2struct"
)

lazy val root = project
  .in(file("."))
  .settings(moduleName := "root")
  .settings(noPublish: _*)
  .aggregate(core, cli)


lazy val core = project
  .in(file("core"))
  .settings(moduleName := "core")
  .enablePlugins(JavaAppPackaging)
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "org.json4s" %% "json4s-native" % "4.0.6",
      "org.scala-lang.modules" %% "scala-parser-combinators" % "2.3.0",
      "org.scalacheck" %% "scalacheck" % "1.15.4",
      "org.scalatest" %% "scalatest" % "3.2.15" % "test"
    ))

lazy val cli = project
  .in(file("cli"))
  .settings(moduleName := "cli")
  .enablePlugins(JavaAppPackaging, UniversalPlugin)
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "org.rogach" %% "scallop" % "5.0.0"
    ),
    Compile / mainClass := Some("json2struct.cli.Cli")
  )
  .dependsOn(core)

//Universal / mappings ++= Seq(file("README.md") -> "README.md")
//
