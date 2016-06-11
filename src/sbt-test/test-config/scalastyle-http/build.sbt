import org.scalastyle.sbt.ScalastylePlugin.settings._

enablePlugins(ScalastylePlugin)

(scalastyleConfigUrl in Test) := Some(url("http://www.scalastyle.org/scalastyle_config.xml"))

(scalastyleConfig in Test) := file("project/scalastyle-test-config.xml")

version := "0.1"

scalaVersion := "2.10.0"

