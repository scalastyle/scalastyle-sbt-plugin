sbtPlugin := true

scalaVersion := "2.9.2"

scalacOptions := Seq("-deprecation", "-unchecked")

organization := "com.github.aselab"

name := "scalastyle-sbt-plugin"

version := "0.1.0"

libraryDependencies ++= Seq(
  "org.scalastyle" % "scalastyle_2.9.1" % "0.1.0",
  "org.specs2" %% "specs2" % "1.12.1" % "test"
)

publishTo := Some(Resolver.file("file", file("target/publish")))

publish <<= (publish, name).map {(_, name) =>
  val script = Path.userHome / ".sbt/publish"
  if (script.exists)
    "%s %s %s".format(script.getAbsolutePath, file("target/publish").getAbsolutePath, name) !
}
