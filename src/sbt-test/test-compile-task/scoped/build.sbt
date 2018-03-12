version := "0.1"

scalaVersion := "2.10.0"

inConfig(Test) {
  Seq(
    compile := {
      val file = target.value / "compile"
      IO.write(file, "compile")
      compile.value
    },
    scalastyle := {
      val file = target.value / "scalastyle"
      IO.write(file, "scalastyle")
    }
  )
}
