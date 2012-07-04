package com.github.aselab.scalastyle

import sbt._
import org.scalastyle._
import java.util.Date

case class ScalaStyle(messages: List[Message[FileSpec]]) {

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

  def toCheckStyleFormat =
    <checkstyle version="5.0">{
      messages.collect {
        case StyleError(file, clazz, key, level, args, line, column, message) =>
          ScalaStyle.Alert(level.name, clazz, sbt.file(file.name), message.getOrElse(key), line.get, column)
        case StyleException(file, clazz, message, stacktrace, line, column) =>
          ScalaStyle.Alert("error", clazz.orNull, sbt.file(file.name), message, line.get, column)
      }.groupBy(_.file.absolutePath).map { case (k, v) =>
        <file name={k}>{
          v.map { e =>
            e.column.map { column =>
              <error line={e.line.toString} column={column.toString}
                severity={e.warnLevel} message={e.message} source={e.clazz.toString}/>
            }.getOrElse {
              <error line={e.line.toString} severity={e.warnLevel}
                message={e.message} source={e.clazz.toString}/>
            }
          }
        }</file>
      }
    }</checkstyle>
}

object ScalaStyle {

  case class Alert(warnLevel: String, clazz: Class[_ <: Checker[_]],
    file: File, message: String, line: Int, column: Option[Int])

  def apply(config: File, sourceDir: File): ScalaStyle = {
    val configuration = ScalastyleConfiguration.readFromXml(config.absolutePath)
    ScalaStyle(new ScalastyleChecker().checkFiles(configuration, Directory.getFiles(sourceDir)))
  }
}

