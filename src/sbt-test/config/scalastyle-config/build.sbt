import org.scalastyle.{ScalastyleConfiguration, ConfigurationChecker}

scalastyleConfig := Some(ScalastyleConfiguration(
  name = "Scalastyle standard configuration",
  checks = List(
    ConfigurationChecker[org.scalastyle.file.FileLengthChecker](
      enabled = true,
      parameters = Map("maxFileLength" -> "5")
    ),
    ConfigurationChecker[org.scalastyle.scalariform.ObjectNamesChecker](
      enabled = true,
      parameters = Map("regex" -> """[A-Z][A-Za-z]*""")
    )
  )
))

version := "0.1"
 
scalaVersion := "2.10.0"
 
