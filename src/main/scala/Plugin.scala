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
import sbt.Compile
import sbt.ConfigKey.configurationToKey
import sbt.File
import sbt.IO
import sbt.InputKey
import sbt.Keys.scalaSource
import sbt.Keys.streams
import sbt.Keys.target
import sbt.Logger
import sbt.Plugin
import sbt.Project
import sbt.Scoped.t3ToTable3
import sbt.Scoped.t6ToTable6
import sbt.SettingKey
import sbt.TaskKey
import sbt.file
import sbt.inputTask
import sbt.richFile
import sbt.std.TaskStreams
import sbt.ScopedKey
import com.typesafe.config.ConfigFactory

object ScalastylePlugin extends Plugin {
  import PluginKeys._ // scalastyle:ignore import.grouping underscore.import

  val Settings = Seq(
    scalastyleTarget <<= target(_ / "scalastyle-result.xml"),
    config := file("scalastyle-config.xml"),
    failOnError := true,
    scalastyle <<= inputTask {
      (argTask: TaskKey[Seq[String]]) => {
        (argTask, config, failOnError, scalaSource in Compile, scalastyleTarget, streams) map {
          (args, config, failOnError, sourceDir, output, streams) => Tasks.doScalastyle(args, config, failOnError, sourceDir, output, streams)
        }
      }
    },
    generateConfig <<= inputTask {
      (args: TaskKey[Seq[String]]) => {
        (args, config, streams) map {
          (args, config, streams) => Tasks.doGenerateConfig(args, config, streams)
        }
      }
    }
  )
}

object PluginKeys {
  lazy val scalastyle = InputKey[Unit]("scalastyle")
  lazy val scalastyleTarget = SettingKey[File]("scalastyle-target")
  lazy val config = SettingKey[File]("scalastyle-config")
  lazy val failOnError = SettingKey[Boolean]("scalastyle-fail-on-error")
  lazy val generateConfig = InputKey[Unit]("scalastyle-generate-config")
}

object Tasks {
  def doScalastyle(args: Seq[String], config: File, failOnError: Boolean, sourceDir: File, output: File,
    streams: TaskStreams[ScopedKey[_]]): Unit = {
    val logger = streams.log
    if (config.exists) {
      val messages = runScalastyle(config, sourceDir)

      saveToXml(messages, output.absolutePath)

      val warnError = args.exists(_ == "w")
      val result = printResults(logger, messages, quiet = args.exists(_ == "q"),
                                warnError = warnError)
      logger.success("created: %s".format(target))

      def onHasErrors(message: String): Unit = {
        if (failOnError) {
          error(message)
        } else {
          logger.error(message)
        }
      }

      if (result.errors > 0) {
        onHasErrors("exists error")
      } else if (warnError && result.warnings > 0) {
        onHasErrors("exists warning")
      }
    } else {
      sys.error("not exists: %s".format(config))
    }
  }

  def doGenerateConfig(args: Seq[String], config: File, streams: TaskStreams[ScopedKey[_]]): Unit = {
    getFileFromJar(getClass.getResource("/scalastyle-config.xml"), config.absolutePath, streams.log)
  }

  private[this] def runScalastyle(config: File, sourceDir: File) = {
    val configuration = ScalastyleConfiguration.readFromXml(config.absolutePath)
    new ScalastyleChecker().checkFiles(configuration, Directory.getFiles(None, List(sourceDir)))
  }

  private[this] def printResults(logger: Logger, messages: List[Message[FileSpec]], quiet: Boolean = false, warnError: Boolean = false): OutputResult = {
    def now: Long = new Date().getTime
    val start = now
    val outputResult =
      new SbtLogOutput(logger, warnError = warnError).output(messages)
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

  private[this] def saveToXml(messages: List[Message[FileSpec]], path: String)(implicit codec: Codec): Unit = {
    XmlOutput.save(path, codec.charSet.toString, messages)
  }

  private[this] implicit def enumToIterator[A](e: java.util.Enumeration[A]): Iterator[A] = new Iterator[A] {
    def next: A = e.nextElement
    def hasNext: Boolean = e.hasMoreElements
  }

  private[this] def getFileFromJar(url: java.net.URL, destination: String, logger: Logger): Unit = {
    def createFile(jarFile: JarFile, e: JarEntry): Unit = {
      val target = file(destination)

      if (safeToCreateFile(target)) {
        IO.transfer(jarFile.getInputStream(e), target)
        logger.success("created: " + target)
      }
    }

    url.openConnection match {
      case connection: java.net.JarURLConnection => {
        val entryName = connection.getEntryName
        val jarFile = connection.getJarFile

        jarFile.entries.filter(_.getName == entryName).foreach { createFile(jarFile, _) }
      }
      case _ => // nothing
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
class SbtLogOutput[T <: FileSpec](logger: Logger, warnError: Boolean = false)
    extends Output[T] {
  import org.scalastyle.{
    StartWork, EndWork, StartFile, EndFile, StyleError, StyleException,
    Level, ErrorLevel, WarningLevel, InfoLevel, MessageHelper
  }

  private val messageHelper = new MessageHelper(ConfigFactory.load())

  override def message(m: Message[T]): Unit = m match {
    case StartWork() => logger.verbose("Starting scalastyle")
    case EndWork() =>
    case StartFile(file) => logger.verbose("start file " + file)
    case EndFile(file) => logger.verbose("end file " + file)
    case StyleError(file, clazz, key, level, args, line, column, customMessage) =>
      plevel(level)(location(file, line, column) + ": " +
          Output.findMessage(messageHelper, key, args, customMessage))
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
