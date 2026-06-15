name := """stock-market-simulator"""
organization := "pl.edu.pw.stockmarketsimulator"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "3.8.3"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.2" % Test
libraryDependencies ++= Seq(
  jdbc,
  evolutions,
  "org.playframework.anorm" %% "anorm" % "2.11.0",
  "org.postgresql" % "postgresql" % "42.7.11",
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.2" % Test
)
libraryDependencies += "org.mindrot" % "jbcrypt" % "0.4"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "pl.edu.pw.stockmarketsimulator.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "pl.edu.pw.stockmarketsimulator.binders._"

PlayKeys.devSettings ++= {
  val envFile = baseDirectory.value / ".env"

  val env =
    if (!envFile.exists()) {
      Map.empty[String, String]
    } else {
      IO.readLines(envFile)
        .flatMap { line =>
          val trimmed = line.trim

          if (trimmed.isEmpty || trimmed.startsWith("#")) {
            None
          } else {
            trimmed.split("=", 2).toList match {
              case key :: value :: Nil =>
                Some(
                  key.trim ->
                    value.trim
                      .stripPrefix("\"")
                      .stripSuffix("\"")
                )

              case _ =>
                None
            }
          }
        }
        .toMap
    }

  Seq(
    "marketData.mode" ->
      env.getOrElse("MARKET_DATA_MODE", "mock"),

    "marketData.cacheTtlSeconds" ->
      env.getOrElse("MARKET_DATA_CACHE_TTL_SECONDS", "60"),

    "finnhub.apiKey" ->
      env.getOrElse("FINNHUB_API_KEY", "")
  )
}