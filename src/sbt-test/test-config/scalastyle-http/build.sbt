enablePlugins(org.scalastyle.sbt.ScalastylePlugin)

(scalastyleConfigUrl in Test) := Some(url("http://www.scalastyle.org/scalastyle_config.xml"))

version := "0.1"
 
scalaVersion := "2.10.0"
 
