ThisBuild / scalaVersion := "2.13.8"
ThisBuild / organization := "com.example"

val scalaTest = "org.scalatest" %% "scalatest" % "3.2.7"

lazy val hello = (project in file("."))
  .aggregate(helloCore, helloUtil)
  .dependsOn(helloCore % "test->test")
  .settings(
    name := "Hello",
    libraryDependencies += scalaTest % Test
  )

lazy val helloCore = (project in file("core"))
  .settings(
    name := "Hello Core",
    libraryDependencies += scalaTest % Test
  )

lazy val helloTask = taskKey[String]("An custom task")
lazy val worldTask = taskKey[Unit]("Another custom task")

lazy val helloUtil = (project in file("util"))
  .settings(
    helloTask := {
      // println("Hello!")
      "Hello!"
    },
    worldTask := {
      val helloStr = helloTask.value
      println(s"$helloStr World!")
      val logger = streams.value.log
      logger.info("log: hello world~")
    }
  )
