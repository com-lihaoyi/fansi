import mill._, scalalib._, scalajslib._, scalanativelib._, publish._


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
  def ivyDeps = Agg(ivy"com.lihaoyi::sourcecode::0.2.1")
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

}


trait FansiTestModule extends ScalaModule with TestModule {
  def crossScalaVersion: String
  def testFrameworks = Seq("utest.runner.Framework")
  def ivyDeps = Agg(ivy"com.lihaoyi::utest::0.7.4")
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
  object jvm extends Cross[JvmFansiModule]("2.12.10", "2.13.1", "0.24.0-RC1")
  class JvmFansiModule(val crossScalaVersion: String)
    extends FansiMainModule with ScalaModule with FansiModule {
    object test extends Tests with FansiTestModule{
      val crossScalaVersion = JvmFansiModule.this.crossScalaVersion
    }
  }

  object js extends Cross[JsFansiModule](
    ("2.12.10", "0.6.32"), ("2.13.1", "0.6.32"), ("2.12.10", "1.0.0"), ("2.13.1", "1.0.0")
  )
  class JsFansiModule(val crossScalaVersion: String, crossJSVersion: String)
    extends FansiMainModule with ScalaJSModule with FansiModule {
    def offset = os.up
    def scalaJSVersion = crossJSVersion
    object test extends Tests with FansiTestModule{
      def offset = os.up
      val crossScalaVersion = JsFansiModule.this.crossScalaVersion
    }
  }

  object native extends Cross[NativeFansiModule](("2.11.12", "0.3.9"), ("2.11.12", "0.4.0-M2"))
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
