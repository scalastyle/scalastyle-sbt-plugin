import org.scalastyle.sbt.ScalastylePlugin.settings._

enablePlugins(ScalastylePlugin)

scalastyleTarget := file("target/my-scalastyle-output.xml")

version := "0.1"

scalaVersion := "2.10.5"

