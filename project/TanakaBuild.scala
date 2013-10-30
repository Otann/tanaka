import com.mojolly.scalate.ScalatePlugin.Binding
import com.mojolly.scalate.ScalatePlugin.TemplateConfig
import sbt._
import Keys._
import org.scalatra.sbt._
import org.scalatra.sbt.PluginKeys._
import com.mojolly.scalate.ScalatePlugin._
import scala.Some
import ScalateKeys._
import net.virtualvoid.sbt.graph.Plugin.graphSettings

object TanakaBuild extends Build {

  val buildLocalSettings = Seq(
    organization := "org.generalrhetoric",
    name         := "My Scalatra Web App",
    version      := "0.1.0-SNAPSHOT",
    scalaVersion := "2.10.2"
  )

  lazy val project = Project (
    id = "tanaka",
    base = file("."),
    settings = Project.defaultSettings
      ++ ScalatraPlugin.scalatraWithJRebel
      ++ scalateSettings
      ++ scalateLocalSettings
      ++ buildLocalSettings
      ++ graphSettings
      ++ Seq(

      resolvers += Classpaths.typesafeReleases,
      libraryDependencies ++= Dependencies.all

    )
  )

  /////////////////////
  // Settings
  /////////////////////

  override lazy val settings = super.settings ++ buildSettings ++ compileSettings

  val compileSettings = Seq (
    scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked"),
    javacOptions ++= Seq("-Xlint:unchecked", "-encoding", "UTF-8")
  )

  val scalateLocalSettings = Seq(
    scalateTemplateConfig in Compile <<= (sourceDirectory in Compile){ base =>
      Seq(
        TemplateConfig(
          base / "webapp" / "WEB-INF" / "templates",
          Seq.empty,  /* default imports should be added here */
          Seq(
            Binding("context", "_root_.org.scalatra.scalate.ScalatraRenderContext", importMembers = true, isImplicit = true)
          ),  /* add extra bindings here */
          Some("templates")
        )
      )
    }
  )

  object Dependencies {

    object V {
      val scalatra = "2.2.1"
    }

    lazy val all = logging ++ scalatra ++ jetty

    lazy val db = Seq(
      "com.novus" %% "salat" % "1.9.4"
    )

    lazy val logging = Seq(
      "ch.qos.logback" % "logback-classic" % "1.0.6"
    )

    lazy val scalatra = Seq(
      "org.scalatra" %% "scalatra"         % V.scalatra,
      "org.scalatra" %% "scalatra-scalate" % V.scalatra,
      "org.scalatra" %% "scalatra-specs2"  % V.scalatra   % "test"
    )

    lazy val jetty = Seq(
      "org.eclipse.jetty"       % "jetty-webapp"  % "8.1.8.v20121106"     % "container",
      "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container;provided;test" artifacts Artifact("javax.servlet", "jar", "jar")
    )

  }
}
