import mill._, scalalib._, scalajslib._, scalanativelib._, publish._
import $ivy.`de.tototec::de.tobiasroeser.mill.vcs.version::0.3.1-6-e80da7`
import $ivy.`com.github.lolgab::mill-mima::0.0.20`
import de.tobiasroeser.mill.vcs.version.VcsVersion
import com.github.lolgab.mill.mima._
import mill.scalalib.api.ZincWorkerUtil.isScala3

val dottyCommunityBuildVersion = sys.props.get("dottyVersion")

val scalaVersions = Seq("2.11.12", "2.12.17", "2.13.8", "3.1.3") ++ dottyCommunityBuildVersion

val scalaJSVersions = scalaVersions.map((_, "1.10.1"))
val scalaNativeVersions = scalaVersions.map((_, "0.4.5"))

trait MimaCheck extends Mima {
  def mimaPreviousVersions = VcsVersion.vcsState().lastTag.toSeq
}

trait FansiModule extends PublishModule with MimaCheck with CrossScalaModule with PlatformScalaModule {
  def artifactName = "fansi"

  def publishVersion = VcsVersion.vcsState().format()

  // Temporary until the next version of Mima gets released with
  // https://github.com/lightbend/mima/issues/693 included in the release.
  def mimaPreviousArtifacts =
    if(isScala3(crossScalaVersion)) Agg.empty[Dep] else super.mimaPreviousArtifacts()

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
    object test extends Tests with FansiTestModule
  }

  object js extends Cross[JsFansiModule](scalaJSVersions)
  trait JsFansiModule extends FansiModule with ScalaJSModule with Cross.Module2[String, String] {
    def scalaJSVersion = crossValue2
    object test extends Tests with FansiTestModule
  }

  object native extends Cross[NativeFansiModule](scalaNativeVersions)
  trait NativeFansiModule extends FansiModule with ScalaNativeModule with Cross.Module2[String, String]{
    def scalaNativeVersion = crossValue2
    object test extends Tests with FansiTestModule
  }
}
