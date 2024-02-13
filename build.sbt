import sbt._
import sbt.Keys._

val ProjectName = "scala-sse-sample"
val NexusMailRu = ("Sonatype Releases Nexus" at "http://nexus.dev.dmr/repository/maven-mailru").withAllowInsecureProtocol(true)

lazy val buildSettings = inThisBuild(
  Seq(
    resolvers ++= Seq(NexusMailRu),
    organization := "ru.vkpay",
    scalaVersion := "2.13.11"
  )
)

lazy val commonSettings = Seq(
  scalacOptions ++= commonScalacOptions,
  version := "1.0.0"
)

lazy val noPublishSettings = Seq(publish := ((): Unit), publishLocal := ((): Unit), publishArtifact := false)

lazy val currentProject = project
  .in(file("."))
  .withId(ProjectName)
  .settings(moduleName := ProjectName, name := ProjectName)
  .settings(buildSettings)
  .settings(commonSettings)
  .settings(libraryDependencies := dependencies.projectLibs ++ dependencies.compilerPlugins)
  .settings(noPublishSettings)
  .enablePlugins(PackPlugin)
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .settings {
    Compile / console / scalacOptions --= Seq("-Ywarn-unused:imports", "-Xfatal-warnings")
  }


lazy val commonScalacOptions = Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:experimental.macros",
  "-unchecked",
  "-Xfatal-warnings",
  //  "-Ystatistics",
  "-Xlint",
  "-Xlint:-byname-implicit",
  "-Ymacro-annotations",
  "-Ywarn-dead-code",
  "-Ywarn-extra-implicit",
  "-Ywarn-unused:imports",
  "-Ywarn-unused:locals",
  "-Ywarn-unused:params",
  "-Ywarn-unused:patvars",
  "-Ywarn-unused:privates",
  "-Ywarn-value-discard",
  "-Xsource:2.13"
)

ThisBuild / libraryDependencySchemes ++= Seq(
  "org.typelevel" %% "log4cats-core" % "always",
  "io.circe" %% "circe-core" % "always",
  "tf.tofu" %% "glass-core" % "always"
)

addCommandAlias("validate", ";clean;test")

