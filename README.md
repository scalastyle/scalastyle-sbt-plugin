# scalastyle-sbt-plugin

This project is intended to provide SBT 0.12.0 and 0.13.0 plugin support for Scalastyle.
For more information about Scalastyle, see http://www.scalastyle.org.

This plugin uses version 0.4.0 of Scalastyle itself.

## Setup

add following line to `project/plugins.sbt`

    addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.4.0")

    resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"


and, inject plugin settings into project in `build.sbt`:

    org.scalastyle.sbt.ScalastylePlugin.Settings

## Usage

You can check your code by typing `sbt scalastyle`.
The result file is `target/scalastyle-result.xml` (CheckStyle compatible format).

Scalastyle Configuration file is `./scalastyle-config.xml` by default.
To generate default configuration file, by typing `sbt scalastyle-generate-config`.

