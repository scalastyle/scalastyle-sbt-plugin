import org.scalastyle.sbt.ScalastylePlugin.settings._

enablePlugins(ScalastylePlugin)

(scalastyleTarget in Test) := file("target/scalastyle-output.xml")

version := "0.1"

scalaVersion := "2.10.0"

