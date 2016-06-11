import org.scalastyle.sbt.ScalastylePlugin.settings._

enablePlugins(ScalastylePlugin)

scalastyleConfigUrl := Some(url("http://www.scalastyle.org/scalastyle_config.xml"))
scalastyleConfig := file("project/scalastyle_config.xml")
version := "0.1"

scalaVersion := "2.10.0"

