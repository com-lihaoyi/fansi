import mill._, scalalib._, scalajslib._, scalanativelib._, publish._

val dottyVersions = sys.props.get("dottyVersion").toList

val scalaVersions = "2.11.12" :: "2.12.13" :: "2.13.4" :: "3.0.0-M3" :: dottyVersions
val scala2Versions = scalaVersions.filter(_.startsWith("2."))

val scalaJSVersions = for {
  scalaV <- scala2Versions
  scalaJSV <- Seq("0.6.33", "1.4.0")
} yield (scalaV, scalaJSV)

val scalaNativeVersions = for {
  scalaV <- scala2Versions
  scalaNativeV <- Seq("0.4.0")
} yield (scalaV, scalaNativeV)

trait FansiModule extends PublishModule {
  def artifactName = "fansi"

  def publishVersion = "0.2.9"

  def pomSettings = PomSettings(
    description = artifactName(),
    organization = "com.lihaoyi",
    url = "https://github.com/lihaoyi/Fansi",
    licenses = Seq(License.MIT),
    scm = SCM(
      "git://github.com/lihaoyi/Fansi.git",
      "scm:git://github.com/lihaoyi/Fansi.git"
    ),
    developers = Seq(
      Developer("lihaoyi", "Li Haoyi", "https://github.com/lihaoyi")
    )
  )
}
trait FansiMainModule extends CrossScalaModule {
  def millSourcePath = super.millSourcePath / offset
  def ivyDeps = Agg(ivy"com.lihaoyi::sourcecode::0.2.2")
  def offset: os.RelPath = os.rel
  def sources = T.sources(
    super.sources()
      .flatMap(source =>
        Seq(
          PathRef(source.path / os.up / source.path.last),
          PathRef(source.path / os.up / os.up / source.path.last),
        )
      )
  )

  override def docJar =
    if (crossScalaVersion.startsWith("2")) super.docJar
    else T {
      val outDir = T.ctx().dest
      val javadocDir = outDir / 'javadoc
      os.makeDir.all(javadocDir)
      mill.api.Result.Success(mill.modules.Jvm.createJar(Agg(javadocDir))(outDir))
    }

}


trait FansiTestModule extends ScalaModule with TestModule {
  def crossScalaVersion: String
  def testFrameworks = Seq("utest.runner.Framework")
  def ivyDeps = Agg(ivy"com.lihaoyi::utest::0.7.6")
  def offset: os.RelPath = os.rel
  def millSourcePath = super.millSourcePath / os.up

  def sources = T.sources(
    super.sources()
      .++(CrossModuleBase.scalaVersionPaths(crossScalaVersion, s => millSourcePath / s"src-$s" ))
      .flatMap(source =>
        Seq(
          PathRef(source.path / os.up / "test" / source.path.last),
          PathRef(source.path / os.up / os.up / "test" / source.path.last),
        )
      )
      .distinct
  )
}

object fansi extends Module {
  object jvm extends Cross[JvmFansiModule](scalaVersions:_*)
  class JvmFansiModule(val crossScalaVersion: String)
    extends FansiMainModule with ScalaModule with FansiModule {
    object test extends Tests with FansiTestModule{
      val crossScalaVersion = JvmFansiModule.this.crossScalaVersion
    }
  }

  object js extends Cross[JsFansiModule](scalaJSVersions:_*)
  class JsFansiModule(val crossScalaVersion: String, crossJSVersion: String)
    extends FansiMainModule with ScalaJSModule with FansiModule {
    def offset = os.up
    def scalaJSVersion = crossJSVersion
    object test extends Tests with FansiTestModule{
      def offset = os.up
      val crossScalaVersion = JsFansiModule.this.crossScalaVersion
    }
  }

  object native extends Cross[NativeFansiModule](scalaNativeVersions:_*)
  class NativeFansiModule(val crossScalaVersion: String, crossScalaNativeVersion: String)
    extends FansiMainModule with ScalaNativeModule with FansiModule {
    def offset = os.up

    def scalaNativeVersion = crossScalaNativeVersion
    object test extends Tests with FansiTestModule{
      def offset = os.up
      val crossScalaVersion = NativeFansiModule.this.crossScalaVersion
    }
  }
}
