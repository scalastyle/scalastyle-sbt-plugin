package com.github.aselab.scalastyle

import sbt._
import org.scalastyle._
import java.util.Date

case class ScalaStyle(messages: List[Message[FileSpec]]) {
  import ScalaStyle._

  def printResults(isQuiet: Boolean = false) = {
    def now: Long = new Date().getTime
    val start = now
    val outputResult = new TextOutput().output(messages)
    if (!isQuiet) println("Processed " + outputResult.files + " file(s)")
    if (!isQuiet) println("Found " + outputResult.errors + " errors")
    if (!isQuiet) println("Found " + outputResult.warnings + " warnings")
    if (!isQuiet) println("Finished in " + (now - start) + " ms")

    outputResult
  }

  def toCheckStyleFormat = {
    def attr(name: String, value: Option[Any]): xml.MetaData = value.map { v =>
      xml.Attribute("", name, v.toString, xml.Null)
    }.getOrElse(xml.Null)

    <checkstyle version="5.0">{
      messages.collect {
        case StyleError(file, clazz, key, level, args, line, column, message) =>
          Alert(level.name, clazz, sbt.file(file.name), message.getOrElse(key), line, column)
        case StyleException(file, clazz, message, stacktrace, line, column) =>
          Alert("error", clazz.orNull, sbt.file(file.name), message, line, column)
      }.groupBy(_.file.absolutePath).map { case (path, alerts) =>
        <file name={path}>{
          alerts.map {
            case Alert(warnLevel, clazz, file, message, line, column) =>
              <error
                severity={warnLevel}
                message={message}
                source={clazz.toString}
              /> % attr("line", line) % attr("column", column)
          }
        }</file>
      }
    }</checkstyle>
  }
}

object ScalaStyle {

  case class Alert(warnLevel: String, clazz: Class[_ <: Checker[_]],
    file: File, message: String, line: Option[Int], column: Option[Int])

  def apply(config: File, sourceDir: File): ScalaStyle = {
    val configuration = ScalastyleConfiguration.readFromXml(config.absolutePath)
    ScalaStyle(new ScalastyleChecker().checkFiles(configuration, Directory.getFiles(sourceDir)))
  }
}

