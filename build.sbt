import Build.{noPublish, publishSettings}

name := "json2struct"
version := "0.3.0-SNAPSHOT"
organization := "me.yceel.json2struct"
maintainer := "sleefd@gmail.com"

val scala2version = "2.13.12"

val commonSettings = Seq(
  scalaVersion := scala2version
)

lazy val root = project
  .in(file("."))
  .settings(moduleName := "root")
  .settings(noPublish: _*)
  .aggregate(core, cli)


lazy val core = project
  .in(file("core"))
  .enablePlugins(JavaAppPackaging)
  .settings(moduleName := "core")
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
  .enablePlugins(JavaAppPackaging, UniversalPlugin)
  .settings(moduleName := "cli")
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
