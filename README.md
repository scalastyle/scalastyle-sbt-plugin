# scalastyle-sbt-plugin

This project is intended to provide SBT 0.13 plugin support for Scalastyle.
For more information about Scalastyle, see http://www.scalastyle.org. 
For more information on how to use the plugin, see http://www.scalastyle.org/sbt.html.

This plugin uses version 0.8.0 of Scalastyle itself.

# Getting Started 
Add the following line to `project/plugins.sbt` in your project's "project" directory.

```scala
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.9.0")
```

# Configuration
You will need an XML configuration file. The easiest way to get one is to use the scalastyleGenerateConfig command:

```scala
sbt scalastyleGenerateConfig
``` 

This will create a file, `project/scalastyle-config.xml`, with the default settings. The defaults are often 
suitable but you can modify them to suit your project. The file generated is the same as fro `scalastyle`. 
See http://www.scalastyle.org/ for configuration details.

# Checking Your Code
Once configured, you can check your code with the scalastyle command:

```scala 
sbt scalastyle
```

This produces a list of errors on the console, as well as an XML result file, `target/scalastyle-result.xml`, 
in CheckStyle compatible format.

# Configuring The Plugin
The plugin can be configured as follows:

### scalastyleTarget : File
* Specifies the location of an XML file receives the Scalastyle report. Default value is `target/scalastyle-result.xml`

### scalastyleConfig	: File
* Specifies the location fo the	Scalastyle configuration. Default value is `project/scalastyle-config.xml`

### scalastyleConfigURL : Option[URL]
* Optionally provides a URL for the configuration file.
* If this is `None` (the default), the Scalastyle configuration is expected to be provided in `scalstyleConfig`
* If this is `Some(url)`, the Scalastyle configuration is downloaded from the URL provided.
* When specified, the download overwrites the file specified by `scalastyleConfig`
* If the remote source for your config file is secured and requires the passing of tokens in the header
 of the http request or ssh authentication then `scalastyleConfigUrl` will not suffice. A work around is to define 
 your own update task and make scalastyle depend on it in your build.sbt or Build.scala.
 
### scalastyleConfigRefreshHours	: Integer	
* If scalastyleConfigUrl is set, refresh it after this number of hours. 
* Default value is 24.

### scalastyleFailOnError	: Boolean	
* If true, the scalastyle task fails if any messages at error level are output. Default value is false.

# Manual Update
The plugin provides the `scalastyleConfigUpdate` command which allows you to manually pull the Scalastyle configuration
file down from the URL specified by `scalastyleConfigUrl`.  This is handy when you know the source configuration has
changed and you don't want to wait for the next refresh period.

# Using Scalastyle With Test Code
By default, the plugin will run against your test files with the same configuration as your source files except that
the output will be directed to `target/scalastyle-test-output.xml`. If you want different options for your test code,
you can configure the plugin to do that, like this:

```scala
(scalastyleConfig in Test) := baseDirectory.value / "project" / "scalastyle-test-config.xml"
(scalastyleConfigUrl in Test) := Some(url("https://raw.githubusercontent.com/[owner]/[repository/master/[path]?token=[token]")
(scalasylteTarget in Test) := target.value / "my-test-output.xml"

# Downloading Configuration From GitHub
GitHub now provides an access token to allow you to retrieve the plaintext version of a file from a repository, even
a private one. All you need to do is set the `scalastyleConfigUrl` value properly, like this:

```scala
val my_url = "https://raw.githubusercontent.com/[owner]/[repository/master/[path]?token=[token]"
scalastyleConfigUrl := Some(url(my_url)
```

Make the following substitutions in the URL:
* [owner] - the owner of the repository (e.g. scalastyle)
* [repository] - the name of the repository (e.g. scalastyle-sbt-plugin)
* [path] - the path to the `scalastyle-config.xml` in the repository (e.g. project/scalastyle-config.xml)
* [token] - the GitHub access token

# Integration With IntelliJ IDEA
IntelliJ IDEA knows how to apply your scalastyle-config.xml settings live in your editor. It expects the file to be
located in `project/scalastyle-config.xml` or at the base directory level. The plugin defaults the location to the
project directory to enable this integration automatically and so that the base directory level is not cluttered.
