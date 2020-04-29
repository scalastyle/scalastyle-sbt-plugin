resolvers += "sonatype-snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

{
  val pluginVersion = System.getProperty("plugin.version")
  if(pluginVersion == null)
    throw new RuntimeException("""|The system property 'plugin.version' is not defined.
                                  |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
  else addSbtPlugin("org.scalastyle" % "scalastyle-sbt-plugin" % pluginVersion)
}
