import org.scalastyle.sbt.ScalastylePlugin.settings._

enablePlugins(ScalastylePlugin)

(scalastyleFailOnError in Test) := false

version := "0.1"

scalaVersion := "2.10.0"

