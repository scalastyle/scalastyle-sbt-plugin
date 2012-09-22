# scalastyle-sbt-plugin

This project is intended to provide SBT 0.12.0 plugin support for Scalastyle.
For more information about Scalastyle, see http://www.scalastyle.org.

This plugin is still a SNAPSHOT, and uses version 0.2.0-SNAPSHOT of Scalastyle itself.

## Setup

add following line to `project/plugins.sbt`

    addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.2.0-SNAPSHOT")

    resolvers += "sonatype-snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"


and, inject plugin settings into project in `build.sbt`:

    org.scalastyle.sbt.ScalastylePlugin.Settings

## Usage

You can check your code by typing `sbt scalastyle`.
The result file is `target/scalastyle-result.xml` (CheckStyle compatible format).

ScalaStyle Configuration file is `./scalastyle-config.xml` by default.
To generate default configuration file, by typing `sbt scalastyle-generate-config`.

