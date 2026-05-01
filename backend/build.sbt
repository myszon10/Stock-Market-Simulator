name := """stock-market-simulator"""
organization := "pl.edu.pw.stockmarketsimulator"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "3.8.3"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.0" % Test
libraryDependencies ++= Seq(
  jdbc,
  evolutions,
  "org.postgresql" % "postgresql" % "42.7.11"
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "pl.edu.pw.stockmarketsimulator.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "pl.edu.pw.stockmarketsimulator.binders._"
