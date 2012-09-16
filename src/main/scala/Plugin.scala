package com.github.aselab.scalastyle

import sbt._
import Keys._

object ScalaStylePlugin extends Plugin {
  import PluginKeys._

  val Settings = Seq(
    scalaStyle <<= Tasks.scalaStyle,
    generateConfig <<= Tasks.generateConfig,
    scalaStyleTarget <<= (target).map(_ / "scalastyle-result.xml"),
    // TODO: to configuration file(HOCON format).
    config := file("scalastyle-config.xml")
  )
}

object PluginKeys {
  lazy val scalaStyle = InputKey[Unit]("scalastyle")
  lazy val scalaStyleTarget = TaskKey[File]("scalastyle-target")
  lazy val config = SettingKey[File]("scalastyle-config")
  lazy val generateConfig = InputKey[Unit]("scalastyle-generate-config")
}

object Tasks {
  import PluginKeys._

  val scalaStyle: Project.Initialize[sbt.InputTask[Unit]] = inputTask {
    (_, config, scalaSource in Compile, scalaStyleTarget, streams) map { case (args, config, sourceDir, target, streams) =>
      val logger = streams.log
      if (config.exists) {
        val scalaStyle = ScalaStyle(config, sourceDir)
        val result = scalaStyle.printResults(args.exists(_ == "q"))
        logger.success("created: %s".format(target))

        if (result.errors > 0)
          logger.error("exists error")
        else if (args.exists(_ == "w") && result.warnings > 0)
          logger.error("exists warning")
      } else {
        sys.error("not exists: %s".format(config))
      }
    }
  }

  val generateConfig: Project.Initialize[sbt.InputTask[Unit]] = inputTask {
    (_, config, streams) map { case (args, to, streams) =>
      IOUtil.copyJarResourses(getClass.getResource("/scalastyle-config.xml"), to.absolutePath, streams.log)
    }
  }
}

