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

import java.util.Date
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.jar.JarEntry
import java.util.jar.JarFile
import scala.io.Codec
import org.scalastyle.Directory
import org.scalastyle.FileSpec
import org.scalastyle.Message
import org.scalastyle.OutputResult
import org.scalastyle.ScalastyleChecker
import org.scalastyle.ScalastyleConfiguration
import org.scalastyle.Output
import org.scalastyle.XmlOutput
import sbt.Configuration
import sbt.Compile
import sbt.Test
import sbt.ConfigKey.configurationToKey
import sbt.File
import sbt.IO
import sbt.inputKey
import sbt.Keys.scalaSource
import sbt.Keys.streams
import sbt.Keys.target
import sbt.Logger
import sbt.Plugin
import sbt.Process
import sbt.Project
import sbt.Scoped.t3ToTable3
import sbt.Scoped.t6ToTable6
import sbt.settingKey
import sbt.taskKey
import sbt.file
import sbt.inputTask
import sbt.richFile
import sbt.std.TaskStreams
import sbt.url
import sbt.ScopedKey
import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config
import scala.language.implicitConversions
import java.net.URL

object ScalastylePlugin extends Plugin {
  import sbt.complete.DefaultParsers._

  val scalastyle = inputKey[Unit]("Run scalastyle on your code")
  val scalastyleGenerateConfig = taskKey[Unit]("Generate a default configuration files for scalastyle")

  val scalastyleTarget = settingKey[File]("XML output file from scalastyle")
  val scalastyleConfig = settingKey[File]("Scalastyle configuration file")
  val scalastyleConfigUrl = settingKey[Option[URL]]("Scalastyle configuration file as a URL")
  val scalastyleFailOnError = settingKey[Boolean]("If true, Scalastyle will fail the task when an error level rule is violated")
  val scalastyleConfigRefreshHours = settingKey[Integer]("How many hours until next run will fetch the scalastyle-config.xml again if location is a URI.")
  val scalastyleConfigUrlCacheFile = settingKey[String]("If scalastyleConfigUrl is set, it will be cached here")
  val scalastyleSources = settingKey[Seq[File]]("Which sources will scalastyle check")

  def rawScalastyleSettings(): Seq[sbt.Def.Setting[_]] =
    Seq(
      scalastyle := {
        val args: Seq[String] = spaceDelimited("<arg>").parsed
        val scalastyleSourcesV = scalastyleSources.value
        val configV = scalastyleConfig.value
        val configUrlV = scalastyleConfigUrl.value
        val streamsV = streams.value
        val failOnErrorV = scalastyleFailOnError.value
        val scalastyleTargetV = scalastyleTarget.value
        val configRefreshHoursV = scalastyleConfigRefreshHours.value
        val targetV = target.value
        val configCacheFileV = scalastyleConfigUrlCacheFile.value

        Tasks.doScalastyle(args, configV, configUrlV, failOnErrorV, scalastyleSourcesV, scalastyleTargetV, streamsV, configRefreshHoursV, targetV, configCacheFileV)
      },
      scalastyleGenerateConfig := {
        val streamsValue = streams.value
        val configValue = scalastyleConfig.value
        Tasks.doGenerateConfig(configValue, streamsValue)
      }
    )

  override def projectSettings =
    Seq(
      scalastyleConfig := file("scalastyle-config.xml"),
      (scalastyleConfig in Test) := (scalastyleConfig in scalastyle).value,
      scalastyleConfigUrl := None,
      (scalastyleConfigUrl in Test) := None,
      scalastyleConfigUrlCacheFile := "scalastyle-config.xml",
      (scalastyleConfigUrlCacheFile in Test) := "scalastyle-test-config.xml",
      scalastyleConfigRefreshHours := 24,
      (scalastyleConfigRefreshHours in Test) := (scalastyleConfigRefreshHours in scalastyle).value,
      scalastyleTarget := (target.value / "scalastyle-result.xml"),
      (scalastyleTarget in Test) := target.value / "scalastyle-test-result.xml",
      scalastyleFailOnError := true,
      (scalastyleFailOnError in Test) := (scalastyleFailOnError in scalastyle).value,
      scalastyleSources := Seq((scalaSource in Compile).value),
      (scalastyleSources in Test) := Seq((scalaSource in Test).value
    ) ++
    Project.inConfig(Compile)(rawScalastyleSettings()) ++
    Project.inConfig(Test)(rawScalastyleSettings())
}

object Tasks {
  def doScalastyle(args: Seq[String], config: File, configUrl: Option[URL], failOnError: Boolean, scalastyleSources: Seq[File], scalastyleTarget: File,
                      streams: TaskStreams[ScopedKey[_]], refreshHours: Integer, target: File, urlCacheFile: String): Unit = {
    val logger = streams.log
    val quietArg = "q"
    val warnErrorArg = "w"
    val supportedArgs = Set(quietArg, warnErrorArg)

    val quiet = args.contains(quietArg)
    val warnError = args.contains(warnErrorArg)

    def onHasErrors(message: String): Unit = {
      if (failOnError) {
        sys.error(message)
      } else {
        logger.error(message)
      }
    }

    def getConfigFile(targetDirectory: File, configUrl: Option[URL], config: File, outputFile: String): File = {
      val f = configUrl match {
        case Some(url) => {
          val targetConfigFile = target / outputFile
          if (!targetConfigFile.exists || MILLISECONDS.toHours((new Date()).getTime - targetConfigFile.lastModified) >= refreshHours) {
            try {
              logger.info("downloading " + url + " to " + targetConfigFile.getAbsolutePath())
              Process.apply(targetConfigFile) #< url ! logger
            } catch {
              case ex: Exception => onHasErrors(s"Unable to download remote config: $ex")
            }
          }
          targetConfigFile
        }
        case None => config
      }

      if (!quiet) {
        logger.info("scalastyle using config " + f.getAbsolutePath())
      }

      f
    }

    def isInProject(sources: Seq[File])(f: File) = {
        val validFile = f.exists() && sources.find(s => f.getAbsolutePath.startsWith(s.getAbsolutePath)).isDefined
        if (!validFile) logger.warn(s"File $f does not exist in project")
        validFile
    }

    def doScalastyleWithConfig(config: File): Unit = {
      val messageConfig = ConfigFactory.load(new ScalastyleChecker().getClass().getClassLoader())
      //streams.log.error("messageConfig=" + messageConfig.root().render())

      val filesToProcess: Seq[File] = args.filterNot(supportedArgs.contains).map(file).filter(isInProject(scalastyleSources)) match {
        case Nil => scalastyleSources
        case files => files
      }

      val messages = runScalastyle(config, filesToProcess)

      saveToXml(messageConfig, messages, scalastyleTarget.absolutePath)

      val result = printResults(messageConfig, logger, messages, quiet = quiet, warnError = warnError)
      if (!quiet) {
        logger.success("created output: %s".format(target))
      }

      if (result.errors > 0) {
        onHasErrors("errors exist")
      } else if (warnError && result.warnings > 0) {
        onHasErrors("warnings exist")
      }
    }

    val configFileToUse = getConfigFile(target, configUrl, config, urlCacheFile)
    if (configFileToUse.exists) {
      doScalastyleWithConfig(configFileToUse)
    } else {
      sys.error("config does not exist: %s".format(configFileToUse))
    }
  }

  def doGenerateConfig(config: File, streams: TaskStreams[ScopedKey[_]]): Unit = {
    getFileFromJar(getClass.getResource("/scalastyle-config.xml"), config.absolutePath, streams.log)
  }

  private[this] def runScalastyle(config: File, filesToProcess: Seq[File]) = {
    val configuration = ScalastyleConfiguration.readFromXml(config.absolutePath)
    new ScalastyleChecker().checkFiles(configuration, Directory.getFiles(None, filesToProcess, Nil))
  }

  private[this] def printResults(config: Config, logger: Logger, messages: List[Message[FileSpec]], quiet: Boolean = false, warnError: Boolean = false): OutputResult = {
    def now: Long = new Date().getTime
    val start = now
    val outputResult =
      new SbtLogOutput(config, logger, warnError = warnError).output(messages)
    // scalastyle:off regex
    if (!quiet) {
      logger.info("Processed " + outputResult.files + " file(s)")
      logger.info("Found " + outputResult.errors + " errors")
      logger.info("Found " + outputResult.warnings + " warnings")
      logger.info("Found " + outputResult.infos + " infos")
      logger.info("Finished in " + (now - start) + " ms")
    }
    // scalastyle:on regex

    outputResult
  }

  private[this] def saveToXml(config: Config, messages: List[Message[FileSpec]], path: String)(implicit codec: Codec): Unit = {
    XmlOutput.save(config, path, codec.charSet.toString, messages)
  }

  private[this] implicit def enumToIterator[A](e: java.util.Enumeration[A]): Iterator[A] = new Iterator[A] {
    def next: A = e.nextElement
    def hasNext: Boolean = e.hasMoreElements
  }

  private[this] def getFileFromJar(url: java.net.URL, destination: String, logger: Logger): Unit = {
    def createFile(jarFile: JarFile, e: JarEntry, target: File): Unit = {
      IO.transfer(jarFile.getInputStream(e), target)
      logger.success("created: " + target)
    }

    val target = file(destination)

    if (safeToCreateFile(target)) {
      url.openConnection match {
        case connection: java.net.JarURLConnection => {
          val entryName = connection.getEntryName
          val jarFile = connection.getJarFile

          jarFile.entries.filter(_.getName == entryName).foreach { e => createFile(jarFile, e, target) }
        }
        case _ => // nothing
      }
    }
  }

  private[this] def safeToCreateFile(file: File): Boolean = {
    def askUser: Boolean = {
      val question = "The file %s exists, do you want to overwrite it? (y/n): ".format(file.getPath)
      scala.Console.readLine(question).toLowerCase.headOption match {
        case Some('y') => true
        case Some('n') => false
        case _ => askUser
      }
    }

    if (file.exists) askUser else true
  }
}

/** Report style warnings prettily to sbt logger.
  *
  * @todo factor with TextOutput from scalastyle Output.scala
  */
private[sbt]
class SbtLogOutput[T <: FileSpec](config: Config, logger: Logger, warnError: Boolean = false)
    extends Output[T] {
  import org.scalastyle.{
    StartWork, EndWork, StartFile, EndFile, StyleError, StyleException,
    Level, ErrorLevel, WarningLevel, InfoLevel, MessageHelper
  }

  private val messageHelper = new MessageHelper(config)

  override def message(m: Message[T]): Unit = m match {
    case StartWork() => logger.verbose("Starting scalastyle")
    case EndWork() =>
    case StartFile(file) => logger.verbose("start file " + file)
    case EndFile(file) => logger.verbose("end file " + file)
    case StyleError(file, clazz, key, level, args, line, column, customMessage) => {
      plevel(level)(location(file, line, column) + ": " +
          Output.findMessage(messageHelper, key, args, customMessage))
    }
    case StyleException(file, clazz, message, stacktrace, line, column) =>
      logger.error(location(file, line, column) + ": " + message)
  }

  private[this]
  def plevel(level: Level)(msg: => String): Unit = level match {
    case ErrorLevel => logger.error(msg)
    case WarningLevel => if (warnError) logger.error(msg) else logger.warn(msg)
    case InfoLevel => logger.info(msg)
  }

  private[this]
  def location(file: T, line: Option[Int], column: Option[Int]): String =
    (file.name +
     line.map(n => ":" + n + column.map(":" + _).getOrElse(""))
         .getOrElse(""))
}
