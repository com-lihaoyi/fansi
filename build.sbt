// shadow sbt-scalajs' crossProject and CrossType from Scala.js 0.6.x
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

val baseSettings = Seq(
  organization := "com.lihaoyi",
  name := "fansi",
  version := "0.2.6",

  scalaVersion := "2.12.8",
  crossScalaVersions := Seq("2.10.7", "2.11.12", "2.12.8", "2.13.0-M5"),
  homepage := Some(url("https://github.com/lihaoyi/fansi")),
  scmInfo := Some(ScmInfo(
    browseUrl = url("https://github.com/lihaoyi/fansi"),
    connection = "scm:git:git@github.com:lihaoyi/fansi.git"
  )),
  licenses := Seq("MIT" -> url("http://www.opensource.org/licenses/mit-license.html")),
  developers += Developer(
    email = "haoyi.sg@gmail.com",
    id = "lihaoyi",
    name = "Li Haoyi",
    url = url("https://github.com/lihaoyi")
  )
)

baseSettings

lazy val fansi = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .settings(baseSettings)
  .settings(
    scalacOptions += "-feature",
    scalacOptions ++= Seq(scalaBinaryVersion.value match {
      case x if x.startsWith("2.12") => "-target:jvm-1.8"
      case _ => "-target:jvm-1.7"
    }),
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "sourcecode" % "0.1.5",
      "com.lihaoyi" %%% "utest" % "0.6.6" % "test"
    ),
    testFrameworks := Seq(new TestFramework("utest.runner.Framework")),
    publishTo := Some("releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
  )
  .nativeSettings(
    scalaVersion := "2.11.12",
    nativeLinkStubs := true
  )

lazy val fansiJVM = fansi.jvm
lazy val fansiJS = fansi.js
lazy val fansiNative = fansi.native
