name := "Nonogram"

version := "0.1"

scalaVersion := "2.13.0"

// Add dependency on ScalaFX library
libraryDependencies += "org.scalafx" % "scalafx_2.13" % "12.0.2-R18"

// Determine OS version of JavaFX binaries
lazy val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux")   => "linux"
  case n if n.startsWith("Mac")     => "mac"
  case n if n.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!")
}

lazy val javaFXModules = Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
libraryDependencies ++= javaFXModules.map( m =>
  "org.openjfx" % s"javafx-$m" % "12.0.2" classifier osName
)

libraryDependencies += "com.squareup.moshi" % "moshi" % "1.8.0"

scalacOptions += "-Ymacro-annotations"

libraryDependencies += "org.scalafx" %% "scalafxml-core-sfx8" % "0.5"

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)