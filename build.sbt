sbtPlugin := true

scalaVersion := "2.9.2"

scalacOptions := Seq("-deprecation", "-unchecked")

organization := "org.scalastyle"

name := "scalastyle-sbt-plugin"

version := "0.2.0"

publishMavenStyle := true

resolvers += "sonatype-snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

publishTo := Some("Sonatype Snapshots Nexus" at "https://oss.sonatype.org/content/repositories/snapshots")

libraryDependencies ++= Seq(
  "org.scalastyle" % "scalastyle_2.9.2" % "0.2.0",
  "org.specs2" %% "specs2" % "1.12.1" % "test"
)

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) 
    Some("snapshots" at nexus + "content/repositories/snapshots") 
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

org.scalastyle.sbt.ScalastylePlugin.Settings

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>http://www.scalastyle.org</url>
  <licenses>
    <license>
      <name>Apache 2.0</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>scm:git:git@github.com:scalastyle/scalastyle.git</url>
    <connection>scm:git:git@github.com:scalastyle/scalastyle.git</connection>
  </scm>
  <developers>
    <developer>
      <id>matthewfarwell</id>
      <name>Matthew Farwell</name>
      <url>http://www.farwell.co.uk</url>
    </developer>
  </developers>)
