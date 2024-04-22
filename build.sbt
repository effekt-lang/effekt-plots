val scala3Version = "3.4.1"

lazy val root = project
  .in(file("."))
  .enablePlugins(ScalaJSPlugin)
  .enablePlugins(ScalablyTypedConverterPlugin)
  .settings(
    name := "Effekt plots",
    version := "0.1.0-SNAPSHOT",

    Compile / npmDependencies ++= Seq(
      "chart.js" -> "2.9.4",
      // we can't use newer chart.js since @types/chart.js is out of date
      "@types/chart.js" -> "2.9.41",
    ),

    libraryDependencies += "com.raquo" %%% "laminar" % "16.0.0",

    scalaVersion := scala3Version,
    scalaJSUseMainModuleInitializer := true,
  )
