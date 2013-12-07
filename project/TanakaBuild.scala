import sbt._
import Keys._
import sbtassembly.Plugin._
import sbtassembly.Plugin.AssemblyKeys._
import spray.revolver.RevolverPlugin._
import twirl.sbt.TwirlPlugin._

object TanakaBuild extends Build {

  val buildLocalSettings = Seq(
    organization := "com.owlunit",
    name         := "Test App",
    version      := "0.1.0-SNAPSHOT",
    scalaVersion := "2.10.3"
  )

  lazy val project = Project (
    id = "tanaka",
    base = file("."),
    settings = Project.defaultSettings
      ++ Revolver.settings
      ++ Twirl.settings
      ++ assemblySettings
      ++ buildLocalSettings
      ++ Seq(

      resolvers ++= customResolvers,
      libraryDependencies ++= Dependencies.all

    )
  )

  /////////////////////
  // Settings
  /////////////////////

  override lazy val settings = super.settings ++ compileSettings

  val compileSettings = Seq (
    scalacOptions ++= Seq("-encoding", "utf8", "-deprecation", "-unchecked"),
    javacOptions  ++= Seq("-Xlint:unchecked", "-encoding", "UTF-8")
  )


  val customResolvers = Seq(
    Classpaths.typesafeReleases,
    "Sonatype Snapshots"    at "http://oss.sonatype.org/content/repositories/snapshots",
    "Sonatype Releases"     at "http://oss.sonatype.org/content/repositories/releases",
    "Spray.IO Repo"         at "http://repo.spray.io/"
  )

  object Dependencies {

    object V {
      val akka  = "2.2.3"
      val spray = "1.1.0"
    }

    lazy val all = Seq(
      "com.novus"           %% "salat"            % "1.9.4", // exclude("org.scala-lang", "scalap"),

      "io.spray"            %  "spray-can"        % V.spray,
      "io.spray"            %  "spray-routing"    % V.spray,
      "io.spray"            %  "spray-testkit"    % V.spray,

      "com.typesafe.akka"   %%  "akka-actor"      % V.akka,
      "com.typesafe.akka"   %%  "akka-testkit"    % V.akka,
      "com.typesafe.akka"   %%  "akka-slf4j"      % V.akka,

      "ch.qos.logback"      %   "logback-classic" % "1.0.6",
      "org.specs2"          %%  "specs2"          % "2.2.3" % "test"
    )

  }
}
