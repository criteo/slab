lazy val commonSettings = Seq(
  organization := "com.criteo",
  version := "0.4.9",
  scalaVersion := "2.12.2",
  crossScalaVersions := Seq("2.11.8", "2.12.2"),
  credentials += Credentials(
    "Sonatype Nexus Repository Manager",
    "oss.sonatype.org",
    "criteo-oss",
    sys.env.getOrElse("SONATYPE_PASSWORD", "")
  ),
  pgpPassphrase := sys.env.get("SONATYPE_PASSWORD").map(_.toArray),
  pgpSecretRing := file(".travis/secring.gpg"),
  pgpPublicRing := file(".travis/pubring.gpg"),
  pomExtra in Global := {
    <url>https://github.com/criteo/lolhttp</url>
      <licenses>
        <license>
          <name>Apache 2</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
        </license>
      </licenses>
      <scm>
        <connection>scm:git:github.com/criteo/slab.git</connection>
        <developerConnection>scm:git:git@github.com:criteo/slab.git</developerConnection>
        <url>github.com/criteo/slab</url>
      </scm>
      <developers>
        <developer>
          <name>Sheng Ran</name>
          <email>s.ran@criteo.com</email>
          <url>https://github.com/jedirandy</url>
          <organization>Criteo</organization>
          <organizationUrl>http://www.criteo.com</organizationUrl>
        </developer>
      </developers>
  }
)

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    name := "slab",
    libraryDependencies ++= Seq(
      "org.json4s" %% "json4s-native" % "3.4.2",
      "org.scalatest" %% "scalatest" % "3.0.1" % Test,
      "org.mockito" % "mockito-core" % "2.7.0" % Test,
      "org.slf4j" % "slf4j-api" % "1.7.25",
      "com.criteo.lolhttp" %% "lolhttp" % "0.6.1",
      "com.github.cb372" %% "scalacache-core" % "0.9.4",
      "com.github.cb372" %% "scalacache-caffeine" % "0.9.4",
      "com.chuusai" %% "shapeless" % "2.3.2"
    )
  )

lazy val example = (project in file("example"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-simple" % "1.7.25"
    )
  )
  .settings(
    Option(System.getenv().get("GENERATE_EXAMPLE_DOC")).map { _ =>
      Seq(
        autoCompilerPlugins := true,
        addCompilerPlugin("com.criteo.socco" %% "socco-plugin" % "0.1.6"),
        scalacOptions := Seq(
          "-P:socco:out:examples",
          "-P:socco:package_scala:http://www.scala-lang.org/api/current/",
          "-P:socco:package_lol.http:https://criteo.github.io/lolhttp/api/",
          "-P:socco:package_com.criteo.slab:https://criteo.github.io/slab/api/"
        )
      )
    }.getOrElse(Nil): _*
  )
  .dependsOn(root)

lazy val buildWebapp = taskKey[Unit]("build webapp")

buildWebapp := {
  "npm install" !

  "npm run build -- -p --env.out=target/scala-2.11/classes" !

  "npm run build -- -p --env.out=target/scala-2.12/classes" !
}

packageBin in Compile <<= (packageBin in Compile) dependsOn buildWebapp
