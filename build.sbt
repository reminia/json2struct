import Build.*

name := "json2struct"

val projectVersion = "0.4.1"
val scala2version = "2.13.12"
val javaVersion = "11"
val apiDockerVersion = projectVersion

Global / onChangedBuildSource := ReloadOnSourceChanges

val commonSettings = Seq(
  version := projectVersion,
  scalaVersion := scala2version,
  maintainer := "sleefd@gmail.com",
  organization := "me.yceel.json2struct",
  javacOptions := Seq("-source", javaVersion, "-target", javaVersion),
  scalacOptions ++= Seq("-Xsource:3")
)

lazy val root = project
  .in(file("."))
  .settings(moduleName := "root")
  .settings(noPublish *)
  .settings(commonSettings)
  .aggregate(core, cli, api)

lazy val core = project
  .in(file("core"))
  .settings(moduleName := "core")
  .enablePlugins(JavaAppPackaging)
  .settings(commonSettings *)
  .settings(publishSettings *)
  .settings(
    libraryDependencies ++= Seq(
      "org.json4s" %% "json4s-native" % "4.0.6",
      "org.scala-lang.modules" %% "scala-parser-combinators" % "2.3.0",
      "org.scalacheck" %% "scalacheck" % "1.15.4",
      typesafeConfig,
      "org.scalatest" %% "scalatest" % "3.2.15" % "test"
    ))

lazy val cli = project
  .in(file("cli"))
  .settings(moduleName := "cli")
  .enablePlugins(JavaAppPackaging, UniversalPlugin)
  .settings(commonSettings *)
  .settings(publishSettings *)
  .settings(
    libraryDependencies ++= Seq(
      "org.rogach" %% "scallop" % "5.0.0"
    ),
    mainClass := Some("json2struct.cli.Cli"),
    Universal / mappings ++= Seq(file("cli/README.md") -> "README.md"),
    bashScriptExtraDefines ++= Seq(
      "export JSON2STRUCT_HOME=$app_home/../"
    ),
  )
  .dependsOn(core)

lazy val api = project
  .in(file("api"))
  .settings(moduleName := "api")
  .enablePlugins(JavaServerAppPackaging, UniversalPlugin, DockerPlugin)
  .settings(commonSettings *)
  .settings(publishSettings *)
  .settings(
    resolvers += "Akka repo".at("https://repo.akka.io/maven"),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream" % "2.9.0",
      typesafeConfig
    ))
  .settings(
    mainClass := Some("json2struct.api.Server"),
    Universal / mappings ++= Seq(file("api/README.md") -> "README.md"),
    bashScriptExtraDefines ++= Seq(
      "export JSON2STRUCT_HOME=$app_home/../"
    ),
  )
  .settings(commonDockerSettings *)
  .settings(
    Docker / packageName := "json2struct-api",
    Docker / version := apiDockerVersion,
    dockerCommands := Seq(),
    dockerCommands ++= dockerfile("api/docker/Dockerfile")
  )
  .dependsOn(core)
