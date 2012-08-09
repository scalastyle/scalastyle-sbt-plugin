sbtPlugin := true

scalaVersion := "2.9.1"

scalacOptions := Seq("-deprecation", "-unchecked")

organization := "com.github.aselab"

name := "scalastyle-sbt-plugin"

version := "0.1.0"

libraryDependencies ++= Seq(
  "org.scalastyle" %% "scalastyle" % "0.1.0-SNAPSHOT",
  "org.specs2" %% "specs2" % "1.11" % "test"
)

resolvers ++= Seq(
  "Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
)

publish <<= (publish, name).map {(_, name) =>
  val script = Path.userHome / ".sbt/publish"
  if (script.exists)
    "%s %s %s".format(script.getAbsolutePath, file("target/publish").getAbsolutePath, name) !
}
