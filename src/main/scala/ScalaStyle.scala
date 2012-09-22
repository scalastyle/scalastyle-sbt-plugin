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
import org.scalastyle._
import java.util.Date

case class Scalastyle(messages: List[Message[FileSpec]]) {
  import Scalastyle._

  def printResults(isQuiet: Boolean = false): OutputResult = {
    def now: Long = new Date().getTime
    val start = now
    val outputResult = new TextOutput().output(messages)
    // scalastyle:off regex
    if (!isQuiet) println("Processed " + outputResult.files + " file(s)")
    if (!isQuiet) println("Found " + outputResult.errors + " errors")
    if (!isQuiet) println("Found " + outputResult.warnings + " warnings")
    if (!isQuiet) println("Finished in " + (now - start) + " ms")
    // scalastyle:on regex

    outputResult
  }

}

object Scalastyle {
  case class Alert(warnLevel: String, clazz: Class[_ <: Checker[_]],
    file: File, message: String, line: Option[Int], column: Option[Int])

  def apply(config: File, sourceDir: File): Scalastyle = {
    val configuration = ScalastyleConfiguration.readFromXml(config.absolutePath)
    Scalastyle(new ScalastyleChecker().checkFiles(configuration, Directory.getFiles(sourceDir)))
  }
}

