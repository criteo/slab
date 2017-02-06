
lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.criteo",
      scalaVersion := "2.11.8",
      version := "0.1.0-SNAPSHOT"
    )),
    name := "slab",
    resolvers ++= Seq(
      "Twitter maven" at "http://maven.twttr.com",
      "Finatra Repo" at "http://twitter.github.com/finatra"
    ),
    libraryDependencies ++= Seq(
      "com.twitter.finatra" %% "finatra-http" % "2.1.6",
      "org.json4s" %% "json4s-jackson" % "3.4.2",
      "org.scalatest" %% "scalatest" % "2.2.4",
      "org.mockito" % "mockito-core" % "2.7.0" % Test
    )
  )

lazy val buildWebapp = taskKey[Unit]("build webapp")

buildWebapp := {
  "npm install" !

  "npm run build -- -p --env.out=target/scala-2.11/classes" !
}

packageBin in Compile <<= (packageBin in Compile) dependsOn (buildWebapp)
