package com.github.aselab.scalastyle

import org.specs2.mutable._
import java.io.File

object ScalaStyleSpec extends Specification {
  "ScalaStyle" should {
    "parseLine" in {
      "warning line is Some(Alert)" in {
        "column not exists" in {
          val warningLine =
            "warning file=/path/to/src/main/scala/controllers/HogeController.scala " +
            "message=Header does not match expected text line=1"

          ScalaStyle.Alert(warningLine) mustEqual Some(ScalaStyle.Alert(
            "warning",
            new File("/path/to/src/main/scala/controllers/HogeController.scala"),
            "Header does not match expected text",
            1,
            None
          ))
        }

        "column exists" in {
          val warningLine =
            "warning file=/path/to/src/main/scala/controllers/HogeController.scala " +
            "message=Magic Number line=143 column=69"

          ScalaStyle.Alert(warningLine) mustEqual Some(ScalaStyle.Alert(
            "warning",
            new File("/path/to/src/main/scala/controllers/HogeController.scala"),
            "Magic Number",
            143,
            Some(69)
          ))
        }
      }

      "other line is None" in {
        ScalaStyle.Alert("Found 246 warnings, Finished in 1463 ms") must beNone
      }
    }

    "error line is Some(Alert)" in {
      "column not exists" in {
        val errorLine =
          "error file=/path/to/src/main/scala/controllers/HogeController.scala " +
          "message=Header does not match expected text line=1"

        ScalaStyle.Alert(errorLine) mustEqual Some(ScalaStyle.Alert(
          "error",
          new File("/path/to/src/main/scala/controllers/HogeController.scala"),
          "Header does not match expected text",
          1,
          None
        ))
      }

      "column exists" in {
        val errorLine =
          "error file=/path/to/src/main/scala/controllers/HogeController.scala " +
          "message=Magic Number line=143 column=69"

        ScalaStyle.Alert(errorLine) mustEqual Some(ScalaStyle.Alert(
          "error",
          new File("/path/to/src/main/scala/controllers/HogeController.scala"),
          "Magic Number",
          143,
          Some(69)
        ))
      }
    }
  }
}

