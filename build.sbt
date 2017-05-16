val baseSettings = Seq(
  organization := "com.lihaoyi",
  name := "fansi",
  version := "0.2.4",

  scalaVersion := "2.11.8",
  crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.0"),
  homepage := Some(url("https://github.com/lihaoyi/fansi")),  
  scmInfo := Some(ScmInfo(
    browseUrl = url("https://github.com/lihaoyi/utest"),
    connection = "scm:git:git@github.com:lihaoyi/utest.git"
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

lazy val fansi = crossProject
  .settings(baseSettings)
  .settings(
    scalacOptions ++= Seq(scalaBinaryVersion.value match {
      case x if x.startsWith("2.12") => "-target:jvm-1.8"
      case _ => "-target:jvm-1.7"
    }),
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "sourcecode" % "0.1.3",
      "com.lihaoyi" %%% "utest" % "0.4.4" % "test"
    ),
    testFrameworks := Seq(new TestFramework("utest.runner.Framework")),
    publishTo := Some("releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
  )
  .jsSettings(
    scalaJSUseRhino in Global := false
  )

lazy val fansiJVM = fansi.jvm
lazy val fansiJS = fansi.js
