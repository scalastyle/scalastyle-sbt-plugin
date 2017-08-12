lazy val containsMessage = taskKey[Boolean]("contains message")

lazy val commonSettings = Seq(
  version := "0.1",
  scalaVersion := "2.10.0",
  scalastyleConfig in Test := (baseDirectory in ThisBuild).value / "scalastyle-test-config.xml",
  containsMessage := {
    val search = "File length exceeds"
    val filename = (scalastyleTarget in Test).value
    val lines = sbt.IO.readLines(filename)
    val contains = lines exists (_ contains search)
    if (!contains) {
      sys.error("Could not find " + search + " in " + filename)
    }
    contains
  }
)

lazy val root = (project in file(".")).
  aggregate(sub1, sub2).
  settings(commonSettings: _*).
  settings(containsMessage := {
    true
  })

lazy val sub1 = (project in file("sub1")).
  settings(commonSettings: _*)

lazy val sub2 = (project in file("sub2")).
  settings(commonSettings: _*)
