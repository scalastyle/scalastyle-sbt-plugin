# scalastyle-sbt-plugin

This project is intended to provide SBT 0.12.0 and 0.13.0 plugin support for Scalastyle.
For more information about Scalastyle, see http://www.scalastyle.org.

This plugin uses version 0.5.0 of Scalastyle itself.

## Setup

add following line to `project/plugins.sbt`

    addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.5.0")

    resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"


and, inject plugin settings into project in `build.sbt`:

    org.scalastyle.sbt.ScalastylePlugin.Settings

## Usage

You can check your code by typing `sbt scalastyle`.
The result file is `target/scalastyle-result.xml` (CheckStyle compatible format).

Scalastyle Configuration file is `./scalastyle-config.xml` by default. You can change this by changing the `config` setting (a `File`).
To generate default configuration file, by typing `sbt scalastyle-generate-config`.

### Remote Configuration Files

If you want to use a remote configuration file, specify `scalastyleConfig` (an `Option[String]`) as a URI (`http(s)://` or `file://`) in your build:

    import org.scalastyle.sbt.ScalastylePlugin
    import org.scalastyle.sbt.PluginKeys.scalastyleConfigUrl

    lazy val mySettings = ScalastylePlugin.Settings ++ Seq(
      scalastyleConfig := Some("https://raw.githubusercontent.com/scalastyle/scalastyle-sbt-plugin/master/scalastyle-config.xml")
    )

Now this config will be downloaded locally to `target/scalastyle-config.xml`. If it is set to `None`, then this behavior will be ignored. You can also set `scalastyleConfig` to any path: for example, `scalastyleConfig := Some("scalastyle-config.xml")` will use the config file in your project root. This is equivalent to the aforementioned `config` setting, and it will take precedence over that setting if specified.

By default, every 24 hours the remote config will be fetched again so as to keep you up to date. If you want to change that, set `scalastyleConfigUrlRefreshHours` in your build. It is a measure of hours until the next fetch (0 will cause it to always happen.)

