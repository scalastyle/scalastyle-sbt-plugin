# scalastyle-sbt-plugin

This project is intended to provide SBT 0.11.x plugin support for Scalastyle.
For more information about Scalastyle, see https://github.com/scalastyle/scalastyle.

## Setup

add following line to `project/plugins.sbt`

```scala
  addSbtPlugin("com.github.aselab" %% "scalastyle-sbt-plugin" % "0.1.0-SNAPSHOT")

  resolvers ++= Seq(
    "aselab repo" at "http://aselab.github.com/maven/",
    "Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  )
```

and, inject plugin settings into project in `build.sbt`:

```scala
  import com.github.aselab.scalastyle._

  ScalaStylePlugin.Settings
```

## Usage

You can check your code by typing `sbt scalastyle`.
The result file is `target/scalastyle-result.xml` (CheckStyle compatible format).

ScalaStyle Configuration file is `./scalastyle-config.xml` by default.
To generate default configuration file, by typing `sbt scalastyle-generate-config`.

