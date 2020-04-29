import scala.concurrent.duration._

version := "0.1"

scalaVersion := "2.10.0"

inConfig(Compile) {
  Seq(
    compile := {
      val log = streams.value.log
      log.debug("Pausing compile task for 1 second")
      Thread.sleep(1.second.toMillis)
      log.debug("Checking if scalastyle has already executed")
      val file = target.value / "scalastyle"
      if (file.exists()) {
        throw new RuntimeException("scalastyle task executed before or during the compile task")
      }
      compile.value
    },
    scalastyle := {
      val log = streams.value.log
      log.debug("Running scalastyle")
      val file = target.value / "scalastyle"
      IO.write(file, "scalastyle")
    }
  )
}
