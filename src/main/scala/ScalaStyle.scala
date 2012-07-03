package com.github.aselab.scalastyle

import sbt._

case class ScalaStyle(errors: List[ScalaStyle.Alert]) {
  def toCheckStyleFormat =
    """<?xml version="1.0" encoding="UTF-8"?>
    """ + <checkstyle version="5.0">{
      errors.groupBy(_.file.absolutePath).map { case (k, v) =>
        <file name={k}>{
          v.map { e =>
            e.column.map { column =>
              <error line={e.line.toString} column={column.toString}
                severity={e.warnLevel} message={e.message} source="org.scalastyle.Main"/>
            }.getOrElse {
              <error line={e.line.toString} severity={e.warnLevel}
                message={e.message} source="org.scalastyle.Main"/>
            }
          }
        }</file>
      }
    }</checkstyle>.toString
}

object ScalaStyle {
  import org.scalastyle.Main.main

  def apply(config: File, sourceDir: File): ScalaStyle = ScalaStyle(
    SecurityUtil.notExit {
      main(Array("-c", config.absolutePath, sourceDir.absolutePath))
    }.split("\n").toList.flatMap(Alert(_))
  )

  case class Alert(warnLevel: String, file: File, message: String, line: Int, column: Option[Int] = None)

  object Alert {
    val lineRegex = """(.*) file=(.*) message=(.*) line=(\d+)""".r
    val columnRegex = """(.*) file=(.*) message=(.*) line=(\d+) column=(\d+)""".r

    def apply(line: String): Option[Alert] = Option(line match {
      case lineRegex(warnLevel, f, message, line) =>
        Alert(warnLevel, file(f), message, line.toInt)
      case columnRegex(warnLevel, f, message, line, column) =>
        Alert(warnLevel, file(f), message, line.toInt, Some(column.toInt))
      case _ => null
    })
  }
}

