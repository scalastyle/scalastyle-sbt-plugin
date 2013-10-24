// Copyright (C) 2011-2012 the original author or authors.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.scalastyle.sbt

import sbt._
import Keys._
import org.scalastyle.XmlOutput

object ScalastylePlugin extends Plugin {
  import PluginKeys._

  val Settings = Seq(
    scalastyle <<= Tasks.scalastyle,
    generateConfig <<= Tasks.generateConfig,
    scalastyleTarget <<= (target).map(_ / "scalastyle-result.xml"),
    // TODO: to configuration file(HOCON format).
    config := file("scalastyle-config.xml"),
    failOnError := true
  )
}

object PluginKeys {
  lazy val scalastyle = InputKey[Unit]("scalastyle")
  lazy val scalastyleTarget = TaskKey[File]("scalastyle-target")
  lazy val config = SettingKey[File]("scalastyle-config")
  lazy val failOnError = SettingKey[Boolean]("scalastyle-fail-on-error")
  lazy val generateConfig = InputKey[Unit]("scalastyle-generate-config")
}

object Tasks {
  import PluginKeys._

  val scalastyle: Project.Initialize[sbt.InputTask[Unit]] = inputTask {
    (_, config, failOnError, scalaSource in Compile, scalastyleTarget, streams) map {
      case (args, config, failOnError, sourceDir, target, streams) => {
        val logger = streams.log
        if (config.exists) {
          val scalastyle = Scalastyle(config, sourceDir)

          scalastyle.saveToXml(target.absolutePath)

          val result = scalastyle.printResults(args.exists(_ == "q"))
          logger.success("created: %s".format(target))

          def onHasErrors(message: String) {
            if (failOnError) error(message)
            else logger.error(message)
          }

          if (result.errors > 0) {
            onHasErrors("exists error")
          } else if (args.exists(_ == "w") && result.warnings > 0) {
            onHasErrors("exists warning")
          }
        } else {
          sys.error("not exists: %s".format(config))
        }
      }
    }
  }

  val generateConfig: Project.Initialize[sbt.InputTask[Unit]] = inputTask {
    (_, config, streams) map { case (args, to, streams) =>
      IOUtil.copyJarResources(getClass.getResource("/scalastyle-config.xml"), to.absolutePath, streams.log)
    }
  }
}

