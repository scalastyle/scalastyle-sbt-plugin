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

object IOUtil {
  implicit def enumToIterator[A](e : java.util.Enumeration[A]): Iterator[A] = new Iterator[A] {
    def next: A = e.nextElement
    def hasNext: Boolean = e.hasMoreElements
  }

  def copyJarResources(url: java.net.URL, destination: String, logger: Logger) {
    url.openConnection match {
      case connection: java.net.JarURLConnection =>
        val entryName = connection.getEntryName
        val jarFile = connection.getJarFile
        jarFile.entries.filter(_.getName.startsWith(entryName)).foreach { e =>
          val fileName = e.getName.drop(entryName.size)
          val target = file(destination) / fileName
          val message = if (target.exists) "overrided: " else "created: "
          if (!e.isDirectory) {
            if (safeToCreateFile(target)) {
              IO.transfer(jarFile.getInputStream(e), target)
              logger.success(message + target)
            }
          } else {
            IO.createDirectory(target)
            if (!target.exists) logger.success("created: " + target)
          }
        }
      }
    }

  def safeToCreateFile(file: File): Boolean = {
    def askUser: Boolean = {
      val question = "The file %s exists, do you want to override it? (y/n): ".format(file.getPath)
      scala.Console.readLine(question).toLowerCase.headOption match {
        case Some('y') => true
        case Some('n') => false
        case _ => askUser
      }
    }
    if (file.exists) askUser else true
  }
}

