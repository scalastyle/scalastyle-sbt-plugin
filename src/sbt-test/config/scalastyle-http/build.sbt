val p = (project in file(".")).enablePlugins(ScalastylePlugin)

scalastyleConfigUrl := Some(url("http://www.scalastyle.org/scalastyle_config.xml"))

version := "0.1"
 
scalaVersion := "2.10.0"
 
