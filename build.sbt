sbtPlugin := true

scalacOptions := Seq("-deprecation", "-unchecked")

organization := "org.scalastyle"

name := "scalastyle-sbt-plugin"

version := "0.5.0"

//import com.typesafe.sbt.SbtGit._

//versionWithGit

//git.baseVersion := "0.5.0"

publishMavenStyle := true

//seq(bintrayPublishSettings:_*)

//bintray.Keys.repository in bintray.Keys.bintray := "sbt-plugins"

//bintray.Keys.bintrayOrganization in bintray.Keys.bintray := Some("scalastyle")

//licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))

resolvers += "sonatype-snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"

//resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/dev/repo/"

//publishTo := Some("Sonatype Snapshots Nexus" at "https://oss.sonatype.org/content/repositories/snapshots")

libraryDependencies ++= Seq(
  "org.scalastyle" %% "scalastyle" % "0.5.0"
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
