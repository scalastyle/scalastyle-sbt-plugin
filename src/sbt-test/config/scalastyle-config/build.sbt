import org.scalastyle.sbt.ScalastylePlugin.settings._

enablePlugins(ScalastylePlugin)

scalastyleConfig := file("alternative-config.xml")

version := "0.1"

scalaVersion := "2.10.6"

