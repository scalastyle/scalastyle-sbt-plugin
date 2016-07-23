val p = (project in file(".")).enablePlugins(ScalastylePlugin)

scalastyleConfig := file("foo/scalastyle-config.xml")

version := "0.1"
 
scalaVersion := "2.10.0"
