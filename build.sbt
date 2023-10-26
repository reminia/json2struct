ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "json2struct"
  )

libraryDependencies += "org.json4s" %% "json4s-native" % "4.0.6"
libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.3.0"
libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.15.4"