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
import sbt._
import sbt.Keys._
import sbt.ConfigKey.configurationToKey
import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config
import scala.language.implicitConversions
import java.net.URL

case class ScalastyleContext(
  configFile: File, configUrl : Option[URL], sourceFiles: Seq[File],
  exclusions : Seq[String], targetFile: File,
  failOnError : Boolean, refreshHours : Int, logger : Logger, args : Seq[String]
) {
  val quietArg = "q"
  val warnErrorArg = "w"
  val supportedArgs = Set(quietArg, warnErrorArg)
  val quiet = args.contains(quietArg)
  val warnError = args.contains(warnErrorArg)
}

case class Args(args: Seq[String])

object ScalastylePlugin extends AutoPlugin {
  import sbt.complete.DefaultParsers._

  val scalastyle = inputKey[Unit]("Run scalastyle on your code")
  val scalastyleConfigUpdate = inputKey[Unit]("Manually update your scalastyle config from URL (if configured)")
  val scalastyleGenerateConfig = taskKey[Unit]("Generate a default configuration files for scalastyle")

  object settings {
    val scalastyleTarget = settingKey[File]("XML output file from scalastyle")
    val scalastyleConfig = settingKey[File]("Scalastyle configuration file")
    val scalastyleConfigUrl = settingKey[Option[URL]]("Scalastyle configuration file as a URL")
    val scalastyleFailOnError = settingKey[Boolean]("If true, Scalastyle will fail the task when an error level rule is violated")
    val scalastyleConfigRefreshHours = settingKey[Integer]("How many hours until next run will fetch the scalastyle-config.xml again if location is a URI.")
    val scalastyleSources = settingKey[Seq[File]]("Which sources will scalastyle check")
    val scalastyleExclusions = settingKey[Seq[String]](
      "Regex strings to match file names that should be excluded from checking")
  }

  /** The [[sbt.Setting]]s to add in the scope of each project that activates this AutoPlugin. */
  private def settingDefaults : Seq[Setting[_]] = {
    import settings._
    Seq(
      scalastyleConfig := (baseDirectory.value / "project" / "scalastyle-config.xml"),
      scalastyleConfigUrl := None,
      scalastyleConfigRefreshHours := 24,
      scalastyleFailOnError := true,
      scalastyleTarget := (target.value / "scalastyle-result.xml"),
      scalastyleSources := Seq((scalaSource in Compile).value),
      (scalastyleTarget in Test) := (target.value / "scalastyle-test-result.xml"),
      (scalastyleSources in Test) := Seq((scalaSource in Test).value),
      scalastyleExclusions := Seq.empty[String]
    )
  }

  private def scalastyleTaskSettings : Seq[Setting[_]] = {
    import settings._
    Seq(
      scalastyle := {
        val context = ScalastyleContext(scalastyleConfig.value,
          scalastyleConfigUrl.value, scalastyleSources.value,
          scalastyleExclusions.value,
          scalastyleTarget.value, scalastyleFailOnError.value, scalastyleConfigRefreshHours.value,
          streams.value.log, spaceDelimited("<arg>").parsed
        )
        // streams.value.log.info(s"running doScalastyle($context)")
        Tasks.doScalastyle(context)
      },
      (scalastyle in Test) := {
        val context = ScalastyleContext(
          (scalastyleConfig in Test).value, (scalastyleConfigUrl in Test).value, (scalastyleSources in Test).value,
          (scalastyleExclusions in Test).value,
          (scalastyleTarget in Test).value, (scalastyleFailOnError in Test).value,
          (scalastyleConfigRefreshHours in Test).value, streams.value.log, spaceDelimited("<arg>").parsed
        )
        // streams.value.log.info(s"running doScalastyle($context)")
        Tasks.doScalastyle(context)
      }
    )
  }

  private def updateTaskSettings : Seq[Setting[_]] = {
    import settings._
    Seq(
      scalastyleConfigUpdate := {
        Tasks.doScalastyleConfigUpdate(
          ScalastyleContext(
            scalastyleConfig.value, scalastyleConfigUrl.value,
            scalastyleSources.value, scalastyleExclusions.value,
            scalastyleTarget.value, scalastyleFailOnError.value, scalastyleConfigRefreshHours.value, streams.value.log,
            spaceDelimited("<arg>").parsed
          )
        )
      },
      (scalastyleConfigUpdate in Test) := {
        Tasks.doScalastyleConfigUpdate(
          ScalastyleContext(
            (scalastyleConfig in Test).value, (scalastyleConfigUrl in Test).value,
            (scalastyleSources in Test).value,
            (scalastyleExclusions in Test).value,
            (scalastyleTarget in Test).value, (scalastyleFailOnError in Test).value,
            (scalastyleConfigRefreshHours in Test).value, streams.value.log, spaceDelimited("<arg>").parsed
          )
        )
      }
    )
  }

  private def generateTaskSettings : Seq[Setting[_]] = {
    import settings._
    Seq(
      scalastyleGenerateConfig := {
        Tasks.doGenerateConfig(
          ScalastyleContext(
            scalastyleConfig.value, scalastyleConfigUrl.value,
            scalastyleSources.value, scalastyleExclusions.value,
            scalastyleTarget.value, scalastyleFailOnError.value, scalastyleConfigRefreshHours.value, streams.value.log,
            Seq.empty[String]
          )
        )
      },
      (scalastyleGenerateConfig in Test) := {
        Tasks.doGenerateConfig(
          ScalastyleContext(
            (scalastyleConfig in Test).value, (scalastyleConfigUrl in Test).value,
            (scalastyleSources in Test).value,
            (scalastyleExclusions in Test).value,
            (scalastyleTarget in Test).value, (scalastyleFailOnError in Test).value,
            (scalastyleConfigRefreshHours in Test).value, streams.value.log, Seq.empty[String]
          )
        )
      }
    )
  }

  override def projectSettings: Seq[Setting[_]] = {
    settingDefaults ++ scalastyleTaskSettings ++ updateTaskSettings ++ generateTaskSettings
  }
}

object Tasks {

  def onHasErrors(message: String)(implicit context : ScalastyleContext): Unit = {
    if (context.failOnError) {
      sys.error(message)
    } else {
      context.logger.error(message)
    }
  }

  def doScalastyleConfigUpdate(implicit context : ScalastyleContext) : Unit = {
    import context._
    configUrl match  {
      case Some(url) =>
        try {
          logger.info("downloading " + url + " to " + context.configFile.getAbsolutePath)
          val process = Process.apply(configFile) #< url
          process ! logger
        } catch {
          case ex: Exception => onHasErrors(s"Unable to download remote config: $ex")
        }
      case None =>
        logger.info("Nothing to download. Set scalastyleConfigUrl to Some(\"http://yourhost/your/path\")")
    }
    ()
  }

  def getConfigFile(context : ScalastyleContext): File = {
    import context._
    configUrl.map { url: URL =>
      logger.info("Checking url")
      if (configFile.exists) {
        if (MILLISECONDS.toHours((new Date()).getTime - configFile.lastModified) >= refreshHours) {
          doScalastyleConfigUpdate(context)
        } else {
          logger.info("Not time to update config file from URL")
        }
      } else {
        doScalastyleConfigUpdate(context)
      }
      if (!quiet) {
        logger.info("scalastyle is using config " + configFile.getAbsolutePath)
      }
      configFile
    } getOrElse { logger.info("no url, defaulting to config file") ; configFile }
  }

  def isInProject(sources: Seq[File], logger: Logger)(f: File) : Boolean = {
    val validFile = f.exists() && sources.exists(s => f.getAbsolutePath.startsWith(s.getAbsolutePath))
    if (!validFile) logger.warn(s"File $f does not exist in project")
    validFile
  }

  def doScalastyleWithConfig(implicit context : ScalastyleContext): Unit = {
    import context._
    val messageConfig = ConfigFactory.load(new ScalastyleChecker().getClass.getClassLoader)
    //streams.log.error("messageConfig=" + messageConfig.root().render())

    val filesToProcess: Seq[File] = {
      args.filterNot(supportedArgs.contains).map(file).filter(isInProject(sourceFiles,logger)) match {
        case Nil => sourceFiles
        case files => files
      }
    }

    val messages : List[Message[FileSpec]] =
      runScalastyle(configFile, filesToProcess, exclusions)

    saveToXml(messageConfig, messages, targetFile.absolutePath)

    val result = printResults(messageConfig, messages, context)
    if (!quiet) {
      logger.success("created output: %s".format(targetFile.getCanonicalPath))
    }

    if (result.errors > 0) {
      onHasErrors("errors exist")
    } else if (warnError && result.warnings > 0) {
      onHasErrors("warnings exist")
    }
  }

  def doScalastyle(context : ScalastyleContext) : Unit = {
    getConfigFile(context)
    if (context.configFile.exists) {
      doScalastyleWithConfig(context)
    } else {
      sys.error("config file does not exist: %s".format(context.configFile.getCanonicalPath))
    }
  }

  def doGenerateConfig(context: ScalastyleContext) : Unit = {
    getFileFromJar(getClass.getResource("/scalastyle-config.xml"), context.configFile, context.logger)
  }

  private[this] def runScalastyle(
    config: File, dirsToProcess: Seq[File],
    exclusions: Seq[String]
  ) = {
    val configuration =
      ScalastyleConfiguration.readFromXml(config.absolutePath)
    val unExcluded = Directory.getFiles(None, dirsToProcess, Nil)
    val filesToProcess = unExcluded.filterNot { fileSpec =>
      exclusions.exists(regex =>
        new File(fileSpec.name).getName.matches(regex))
    }
    println(s"unExcluded:\n$unExcluded\nfilesToProcess:$filesToProcess")
    new ScalastyleChecker().checkFiles(configuration, filesToProcess)
  }

  private[this] def printResults(
    sbtConfig: Config, messages: List[Message[FileSpec]], context : ScalastyleContext
  ) : OutputResult = {
    import context._
    def now: Long = new Date().getTime
    val start = now
    val outputResult =
      new SbtLogOutput(sbtConfig, logger, warnError = warnError).output(messages)
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

  private[this] def getFileFromJar(url: java.net.URL, destination: File, logger: Logger): Unit = {
    def createFile(jarFile: JarFile, e: JarEntry, target: File): Unit = {
      IO.transfer(jarFile.getInputStream(e), target)
      logger.success("created: " + target)
    }

    if (safeToCreateFile(destination)) {
      url.openConnection match {
        case connection: java.net.JarURLConnection => {
          val entryName = connection.getEntryName
          val jarFile = connection.getJarFile

          jarFile.entries.filter(_.getName == entryName).foreach { e => createFile(jarFile, e, destination) }
        }
        case _ => // nothing
      }
    }
  }

  private[this] def safeToCreateFile(file: File): Boolean = {
    def askUser: Boolean = {
      val question = "The file %s exists, do you want to overwrite it? (y/n): ".format(file.getPath)
      Option(scala.Console.readLine(question)) match {
        case Some(answer) ⇒
          answer.toLowerCase.headOption match {
            case Some('y') => true
            case Some('n') => false
            case _ => askUser
          }
        case None ⇒
          false
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
    file.name +
     line.map(n => ":" + n + column.map(":" + _).getOrElse(""))
         .getOrElse("")
}
