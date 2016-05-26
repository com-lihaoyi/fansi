scalaVersion := "2.11.6"

crossScalaVersions := Seq("2.10.5", "2.11.8")

organization := "com.lihaoyi"

name := "fansi"

version := "0.1.0"

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "utest" % "0.4.3" % "test",
  "com.lihaoyi" %% "sourcecode" % "0.1.1"
)

testFrameworks := Seq(new TestFramework("utest.runner.Framework"))
