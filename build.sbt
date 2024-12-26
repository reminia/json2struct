import Build.*

name := "json2struct"

val projectVersion = "0.7.1"
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
  .settings(name := "root")
  .settings(noPublish)
  .settings(commonSettings)
  .aggregate(core, cli, api)

lazy val core = project
  .in(file("core"))
  .settings(name := "core")
  .enablePlugins(JavaAppPackaging)
  .settings(commonSettings)
  .settings(publishSettings)
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
  .settings(name := "cli")
  .enablePlugins(JavaAppPackaging, UniversalPlugin)
  .settings(commonSettings)
  .settings(publishSettings)
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
  .settings(name := "api")
  .enablePlugins(JavaServerAppPackaging, UniversalPlugin, DockerPlugin)
  .settings(commonSettings)
  .settings(publishSettings)
  .settings(
    resolvers += "Akka repo".at("https://repo.akka.io/maven"),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream" % "2.9.0",
      "com.amazonaws" % "aws-lambda-java-core" % "1.2.1",
      "com.amazonaws" % "aws-lambda-java-events" % "3.8.0",
      "org.slf4j" % "slf4j-api" % "2.0.12",
      "ch.qos.logback" % "logback-classic" % "1.5.6",
      typesafeConfig
    ))
  .settings(
    mainClass := Some("json2struct.api.Server"),
    Universal / mappings ++= Seq(file("api/README.md") -> "README.md"),
    bashScriptExtraDefines ++= Seq(
      "export JSON2STRUCT_HOME=$app_home/../"
    ),
  )
  .settings(commonDockerSettings)
  .settings(
    Docker / packageName := "json2struct-api",
    Docker / version := apiDockerVersion,
    dockerCommands := Seq(),
    dockerCommands ++= dockerfile("api/docker/Dockerfile")
  ).settings(
    Test / test := Def.sequential(
      (Compile / run).toTask(""),
      curlTest
    ).value
  ).settings(
    assembly / assemblyJarName := "json2struct-api-assembly.jar",
    assembly / assemblyMergeStrategy := {
      case "reference.conf" => MergeStrategy.concat
      case "application.conf" => MergeStrategy.concat
      case PathList("META-INF", _*) => MergeStrategy.discard
      case x => MergeStrategy.first
    }
  ).dependsOn(core)
