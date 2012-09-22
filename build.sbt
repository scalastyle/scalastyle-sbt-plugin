sbtPlugin := true

scalaVersion := "2.9.2"

scalacOptions := Seq("-deprecation", "-unchecked")

organization := "org.scalastyle"

name := "scalastyle-sbt-plugin"

version := "0.2.0-SNAPSHOT"

resolvers += "sonatype-snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

publishTo := Some("Sonatype Snapshots Nexus" at "https://oss.sonatype.org/content/repositories/snapshots")

libraryDependencies ++= Seq(
  "org.scalastyle" % "scalastyle_2.9.2" % "0.2.0-SNAPSHOT",
  "org.specs2" %% "specs2" % "1.12.1" % "test"
)

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) 
    Some("snapshots" at nexus + "content/repositories/snapshots") 
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

org.scalastyle.sbt.ScalaStylePlugin.Settings
