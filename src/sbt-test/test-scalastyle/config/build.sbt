val p = (project in file(".")).enablePlugins(ScalastylePlugin)

version := "0.1"
 
scalaVersion := "2.10.0"

scalastyleConfig in Test := file("scalastyle-test-config.xml")
 
val containsMessage = taskKey[Boolean]("contains message")

containsMessage := {
    val search = "File length exceeds"
    val filename = "target/scalastyle-test-result.xml"
    val lines = sbt.IO.readLines(file(filename))
    val contains = lines.find(s => s.contains(search)).isDefined
    if (!contains) {
        error("Could not find " + search + " in " + filename)
    }
    contains
}
