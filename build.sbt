val scala3Version = "3.4.1"

lazy val root = project
  .in(file("."))
  .enablePlugins(ScalaJSPlugin)
  .enablePlugins(ScalaJSBundlerPlugin)
  .settings(
    name := "Effekt plots",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

	scalaJSUseMainModuleInitializer := true,

	resolvers += "jitpack" at "https://jitpack.io",
	libraryDependencies += "com.github.fdietze.scala-js-d3v4" %%% "scala-js-d3v4" % "6b1fbd9"
  )
