sbtPlugin := true

scalacOptions := Seq("-deprecation", "-unchecked")

organization := "com.reactific"

name := "scalastyle-sbt-plugin"

version := "0.9.1"

scalaVersion := "2.10.5"

publishMavenStyle := true

resolvers += "sonatype-snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"

//resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/dev/repo/"

libraryDependencies ++= Seq(
  "org.scalastyle" %% "scalastyle" % "0.8.0"
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
    <url>scm:git:git@github.com:reactific/sbt-scalastyle-plugin.git</url>
    <connection>scm:git:git@github.com:reactufuc/sbt-scalastyle-plugin.git</connection>
  </scm>
  <developers>
    <developer>
      <id>reid-spencer</id>
      <name>Reid Spencer</name>
      <url>https://github.com/reid-spencer</url>
    </developer>
  </developers>)
