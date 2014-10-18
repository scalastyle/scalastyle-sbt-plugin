//resolvers += "sonatype-snapshots" at "https://oss.sonatype.org/content/repositories/releases/"

//addSbtPlugin("org.scalastyle" % "scalastyle-sbt-plugin" % "0.5.0")

resolvers += "jgit-repo" at "http://download.eclipse.org/jgit/maven"

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.6.2")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.5.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8.3")
