lazy val commonSettings = Seq(
  organization := "com.criteo",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.11.8"
)

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    name := "slab",
    resolvers ++= Seq(
      "Twitter maven" at "http://maven.twttr.com",
      "Finatra Repo" at "http://twitter.github.com/finatra"
    ),
    libraryDependencies ++= Seq(
      "com.twitter.finatra" %% "finatra-http" % "2.1.6",
      "org.json4s" %% "json4s-native" % "3.4.2",
      "joda-time" % "joda-time" % "2.9.7",
      "org.scalatest" %% "scalatest" % "2.2.4" % Test,
      "org.mockito" % "mockito-core" % "2.7.0" % Test
    )
  )

lazy val example = (project in file("example"))
  .settings(commonSettings: _*)
    .settings(
      libraryDependencies ++= Seq(
        "org.slf4j" % "slf4j-simple" % "1.7.21"
      )
    )
  .dependsOn(root)

lazy val buildWebapp = taskKey[Unit]("build webapp")

buildWebapp := {
  "npm install" !

  "npm run build -- -p --env.out=target/scala-2.11/classes" !
}

packageBin in Compile <<= (packageBin in Compile) dependsOn buildWebapp

assemblyMergeStrategy in assembly := {
  case "BUILD" => MergeStrategy.discard
  case PathList("javax", "annotation", xs @ _*) => MergeStrategy.first
  case other => MergeStrategy.defaultMergeStrategy(other)
}
