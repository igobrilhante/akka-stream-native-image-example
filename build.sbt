import sbt.Keys.libraryDependencies

ThisBuild / organization := "com.github.radium226"
ThisBuild / scalaVersion := "2.12.7"
ThisBuild / version      := "0.1-SNAPSHOT"

ThisBuild / scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps",
  "-language:higherKinds",
  "-Ypartial-unification")

val graalVersion = SettingKey[String]("graalVersion", "Graal Version")
graalVersion := "1.0.0-rc14"

val akkaVersion = "2.5.22"

lazy val root = (project in file("."))
  .settings(
    name := "example",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "1.5.0",
      "org.typelevel" %% "cats-effect" % "1.1.0",
    ),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion
    ),
    libraryDependencies ++= Seq(
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
      "org.slf4j" % "slf4j-jdk14" % "1.7.26"
    ),
    libraryDependencies ++= Seq(
      "com.oracle.substratevm" % "svm" % graalVersion.value % Provided,
    ),
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.8"),
    mainClass in assembly := Some("com.github.radium226.example.Main"),
    assemblyJarName in assembly := "example.jar"
  )
