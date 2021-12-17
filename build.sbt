organization := "vdumitrescu"
name         := "kinesis-consumer"
version      := "1.0.0"
scalaVersion := "2.12.10"

val circeVersion = "0.13.0"

libraryDependencies ++= Seq(
  "co.fs2"            %% "fs2-io"                          % "2.2.1",
  "io.circe"          %% "circe-core"                      % circeVersion,
  "io.circe"          %% "circe-generic"                   % circeVersion,
  "io.circe"          %% "circe-generic-extras"            % circeVersion,
  "io.circe"          %% "circe-parser"                    % circeVersion,
  "com.amazonaws"     % "aws-java-sdk-sts"                 % "1.11.741",
  "io.laserdisc"      %% "fs2-aws"                         % "2.28.36",
  "io.laserdisc"      %% "fs2-aws-testkit"                 % "2.28.36" % Test excludeAll ("commons-logging", "commons-logging"),
  "io.chrisdavenport" %% "log4cats-slf4j"                  % "1.0.1",
  "ch.qos.logback"    % "logback-classic"                  % "1.2.3",
  "com.github.scopt"  %% "scopt"                           % "3.7.1"
)

val commonSettings = Seq(
  cancelable  := true,
  fork        := true,
  logBuffered := false,
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-feature",
    "-unchecked",
    "-language:higherKinds",
    "-language:postfixOps",
    "-language:implicitConversions",
    "-Xfatal-warnings",
    "-Ypartial-unification"
  )
)

lazy val root = (project in file("."))
  .enablePlugins(BuildInfoPlugin, JavaAppPackaging)
  .settings(commonSettings)
  .settings(
    buildInfoKeys    := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "com.moda.consumer",
    addCommandAlias("format", ";scalafmt;test:scalafmt;scalafmtSbt"),
    addCommandAlias("checkFormat", ";scalafmtCheck;test:scalafmtCheck;scalafmtSbtCheck"),
    addCommandAlias("build", ";checkFormat;clean;compile;test")
  )

maintainer := "https://github.com/vdumitrescu"
