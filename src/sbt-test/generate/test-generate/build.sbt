scalastyleConfigFile := file("scalastyle-config.xml")

(scalastyleConfigFile in Test) := file("test-scalastyle-config.xml")

version := "0.1"
 
scalaVersion := "2.10.0"
