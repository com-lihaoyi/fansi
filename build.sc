import mill._, scalalib._, scalajslib._, scalanativelib._, publish._
import $ivy.`de.tototec::de.tobiasroeser.mill.vcs.version::0.1.4`
import de.tobiasroeser.mill.vcs.version.VcsVersion
import $ivy.`com.github.lolgab::mill-mima::0.0.9`
import com.github.lolgab.mill.mima._

val dottyVersions = sys.props.get("dottyVersion").toList

val scala2VersionsAndDotty = "2.12.13" :: "2.13.4" :: "2.11.12" :: dottyVersions
val scala30 = "3.0.2"
val scala31 = "3.1.1"

val scalaJSVersions = for {
  scalaV <- scala30 :: scala2VersionsAndDotty
  scalaJSV <- Seq("0.6.33", "1.5.1")
  if scalaV.startsWith("2.") || scalaJSV.startsWith("1.")
} yield (scalaV, scalaJSV)

val scalaNativeVersions = for {
  scalaV <- scala31 :: scala2VersionsAndDotty
  scalaNativeV <- Seq("0.4.3")
} yield (scalaV, scalaNativeV)

trait FansiModule extends PublishModule with Mima {
  def artifactName = "fansi"

  def publishVersion = VcsVersion.vcsState().format()

  def mimaPreviousVersions = VcsVersion.vcsState().lastTag.toSeq

  def pomSettings = PomSettings(
    description = artifactName(),
    organization = "com.lihaoyi",
    url = "https://github.com/lihaoyi/Fansi",
    licenses = Seq(License.MIT),
    versionControl = VersionControl.github(owner = "com-lihaoyi", repo = "fansi"),
    developers = Seq(
      Developer("lihaoyi", "Li Haoyi", "https://github.com/lihaoyi")
    )
  )
}
trait FansiMainModule extends CrossScalaModule {
  def millSourcePath = super.millSourcePath / offset
  def ivyDeps = Agg(ivy"com.lihaoyi::sourcecode::0.2.8")
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


trait FansiTestModule extends ScalaModule with TestModule.Utest {
  def crossScalaVersion: String
  def ivyDeps = Agg(ivy"com.lihaoyi::utest::0.7.11")
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
  object jvm extends Cross[JvmFansiModule](scala30 :: scala2VersionsAndDotty:_*)
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
