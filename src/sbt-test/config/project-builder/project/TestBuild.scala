
import sbt.Keys._
import sbt._
import org.scalastyle.sbt.ScalastylePlugin.settings._
import org.scalastyle.sbt.ScalastylePlugin

object TestBuild extends Build {

  lazy val test = Project("test", file("."))
    .enablePlugins(ScalastylePlugin)
    .settings(
      scalastyleConfigUrl := Some(url("http://www.scalastyle.org/scalastyle_config.xml")),
      scalastyleConfig := file("project/scalastyle_config.xml"),
      version := "0.1",
      scalaVersion := "2.11.8"
    )

  override def rootProject : Option[Project] = Some(test)

}
