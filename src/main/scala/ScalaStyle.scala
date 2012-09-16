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

}

object ScalaStyle {

  case class Alert(warnLevel: String, clazz: Class[_ <: Checker[_]],
    file: File, message: String, line: Option[Int], column: Option[Int])

  def apply(config: File, sourceDir: File): ScalaStyle = {
    val configuration = ScalastyleConfiguration.readFromXml(config.absolutePath)
    ScalaStyle(new ScalastyleChecker().checkFiles(configuration, Directory.getFiles(sourceDir)))
  }
}

