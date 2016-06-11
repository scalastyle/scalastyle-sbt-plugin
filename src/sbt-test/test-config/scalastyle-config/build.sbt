import org.scalastyle.sbt.ScalastylePlugin.settings._

enablePlugins(ScalastylePlugin)

(scalastyleConfig in Test) := file("gggalternative-config.xml")

version := "0.1"

scalaVersion := "2.10.0"

