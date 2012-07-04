package com.github.aselab.scalastyle

import sbt._
import org.scalastyle._


case class ScalaStyle(errors: List[ScalaStyle.Alert]) {
  def toCheckStyleFormat =
    <checkstyle version="5.0">{
      errors.groupBy(_.file.absolutePath).map { case (k, v) =>
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
    ScalaStyle(
      new ScalastyleChecker().checkFiles(configuration, Directory.getFiles(sourceDir)).collect {
        case StyleError(file, clazz, key, level, args, line, column, message) =>
          Alert(level.name, clazz, sbt.file(file.name), message.getOrElse(key), line.get, column)
        case StyleException(file, clazz, message, stacktrace, line, column) =>
          Alert("error", clazz.orNull, sbt.file(file.name), message, line.get, column)
      }
    )
  }
}

