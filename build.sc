import mill._, scalalib._, scalajslib._, scalanativelib._, publish._
import $ivy.`de.tototec::de.tobiasroeser.mill.vcs.version::0.4.0`
import $ivy.`com.github.lolgab::mill-mima::0.0.23`
import de.tobiasroeser.mill.vcs.version.VcsVersion
import com.github.lolgab.mill.mima._
import mill.scalalib.api.ZincWorkerUtil.isScala3

val dottyCommunityBuildVersion = sys.props.get("dottyVersion")

val scalaVersions = Seq("2.11.12", "2.12.18", "2.13.8", "3.1.3") ++ dottyCommunityBuildVersion

trait FansiModule extends PublishModule with Mima with CrossScalaModule with PlatformScalaModule {
  def artifactName = "fansi"

  def publishVersion = VcsVersion.vcsState().format()

  def mimaPreviousVersions = VcsVersion.vcsState().lastTag.toSeq

  def pomSettings = PomSettings(
    description = artifactName(),
    organization = "com.lihaoyi",
    url = "https://github.com/com-lihaoyi/Fansi",
    licenses = Seq(License.MIT),
    versionControl = VersionControl.github(owner = "com-lihaoyi", repo = "fansi"),
    developers = Seq(
      Developer("lihaoyi", "Li Haoyi", "https://github.com/lihaoyi")
    )
  )

  def ivyDeps = Agg(ivy"com.lihaoyi::sourcecode::0.3.0")
}

trait FansiTestModule extends ScalaModule with TestModule.Utest {
  def ivyDeps = Agg(ivy"com.lihaoyi::utest::0.8.1")
}

object fansi extends Module {
  object jvm extends Cross[JvmFansiModule](scalaVersions)
  trait JvmFansiModule extends FansiModule with ScalaModule {
    object test extends ScalaTests with FansiTestModule
  }

  object js extends Cross[JsFansiModule](scalaVersions)
  trait JsFansiModule extends FansiModule with ScalaJSModule{
    def scalaJSVersion = "1.10.1"
    object test extends ScalaJSTests with FansiTestModule
  }

  object native extends Cross[NativeFansiModule](scalaVersions)
  trait NativeFansiModule extends FansiModule with ScalaNativeModule{
    def scalaNativeVersion = "0.4.5"
    object test extends ScalaNativeTests with FansiTestModule
  }
}
