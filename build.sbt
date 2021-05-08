val Http4sVersion = "0.21.5"
val CirceVersion = "0.13.0"
val Specs2Version = "4.10.0"
val LogbackVersion = "1.2.3"

lazy val root = (project in file("."))
  .settings(
    organization := "com.mairo",
    name := "ukl",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.2",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,

      "io.circe" %% "circe-generic" % CirceVersion,
      "io.circe" %% "circe-parser" % CirceVersion,
      "io.circe" %% "circe-optics" % CirceVersion,

      "io.circe" %% "circe-literal" % CirceVersion % "it,test",

      "dev.zio" %% "zio" % "1.0.7",
      "dev.zio" %% "zio-interop-cats" % "3.0.2.0",
      "dev.zio" %% "zio-logging" % "0.5.8",
      "org.reactivemongo" %% "reactivemongo" % "1.0.3" % "provided",

      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "io.chrisdavenport" %% "log4cats-slf4j" % "1.1.1",
      "com.github.pureconfig" %% "pureconfig" % "0.13.0",
      "com.rabbitmq" % "amqp-client" % "5.9.0",
      //      "dev.profunktor" %% "fs2-rabbit" % "3.0.1",

      "com.wix" %% "accord-core" % "0.7.6",

      "mysql" % "mysql-connector-java" % "8.0.11",
      "org.tpolecat" %% "doobie-core" % "0.9.0",
      "org.tpolecat" %% "doobie-hikari" % "0.9.0", // HikariCP transactor.


      "org.scalatest" %% "scalatest" % "3.2.2" % Test,
      "org.scalamock" %% "scalamock" % "4.4.0" % Test,

      "org.specs2" %% "specs2-core" % Specs2Version % "test",


    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
  )

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-language:implicitConversions",
  "-feature",
  "-Xfatal-warnings"
)
