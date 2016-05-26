crossScalaVersions := Seq("2.10.5", "2.11.8")

lazy val fansi = crossProject
  .settings(
    scalaVersion := "2.11.8",
    organization := "com.lihaoyi",
    name := "fansi",
    version := "0.1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "utest" % "0.4.3" % "test",
      "com.lihaoyi" %%% "sourcecode" % "0.1.1"
    ),
    testFrameworks := Seq(new TestFramework("utest.runner.Framework")),
    publishTo := Some("releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2"),

    pomExtra :=
      <url>https://github.com/lihaoyi/fansi</url>
        <licenses>
          <license>
            <name>MIT license</name>
            <url>http://www.opensource.org/licenses/mit-license.php</url>
          </license>
        </licenses>
        <scm>
          <url>git://github.com/lihaoyi/fansi.git</url>
          <connection>scm:git://github.com/lihaoyi/fansi.git</connection>
        </scm>
        <developers>
          <developer>
            <id>lihaoyi</id>
            <name>Li Haoyi</name>
            <url>https://github.com/lihaoyi</url>
          </developer>
        </developers>
  )
  .jsSettings(
    scalaJSUseRhino in Global := false
  )

lazy val fansiJVM = fansi.jvm
lazy val fansiJS = fansi.js
