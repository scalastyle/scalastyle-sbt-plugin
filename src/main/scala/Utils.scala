package com.github.aselab.scalastyle

import sbt._

object IOUtil {
  implicit def enumToIterator[A](e : java.util.Enumeration[A]) = new Iterator[A] {
    def next = e.nextElement
    def hasNext = e.hasMoreElements
  }

  def copyJarResourses(url: java.net.URL, destination: String, logger: Logger) {
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

