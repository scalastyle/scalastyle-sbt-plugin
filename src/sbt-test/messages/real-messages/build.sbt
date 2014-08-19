org.scalastyle.sbt.ScalastylePlugin.Settings

version := "0.1"
 
scalaVersion := "2.10.0"
 
val containsMessage = taskKey[Boolean]("contains message")

containsMessage := {
    val search = "File length exceeds"
    val filename = "target/scalastyle-result.xml"
    val lines = sbt.IO.readLines(file(filename))
    val contains = lines.find(s => s.contains(search)).isDefined
    if (!contains) {
        error("Could not find " + search + " in " + filename)
    }
    contains
}
