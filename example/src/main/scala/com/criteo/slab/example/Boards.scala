package com.criteo.slab.example

import com.criteo.slab.core._

object Boards {

  lazy val webserver = Box(
    "Webserver Alpha",
    Seq(
      makeVersionCheck("web.version", "Version", 100, Status.Success),
      makeLatencyCheck("web.latency", "Latency", 300, Status.Success)
    ),
    takeHighestLevel,
    Some(
      """
        |# Doc for web server (markdown)
        |<br/>
        |## URL
        |- http://example.io
        |- http://10.0.0.1
      """.stripMargin)
  )

  lazy val gateway = Box(
    "Gateway Beta",
    Seq(
      makeLatencyCheck("EU", "EU Gateway latency", 200, Status.Success),
      makeLatencyCheck("US", "US Gateway latency", 300, Status.Success)
    ),
    takeHighestLevel
  )

  lazy val pipelineZeta = Box(
    "Pipeline Zeta",
    Seq(
      makeLatencyCheck("A", "Job A latency", 1000, Status.Success),
      makeLatencyCheck("B", "Job B latency", 2000, Status.Warning),
      makeLatencyCheck("C", "Job C latency", 3000, Status.Error)
    ),
    takeHighestLevel
  )

  lazy val pipelineOmega = Box(
    "Pipeline Omega",
    Seq(
      makeLatencyCheck("A", "Job A latency", 1000, Status.Success),
      makeLatencyCheck("B", "Job B latency", 1000, Status.Success),
      makeLatencyCheck("C", "Job C latency", 1000, Status.Success)
    ),
    takeHighestLevel
  )

  lazy val databaseKappa = Box(
    "Database Kappa",
    Seq(
      makeLatencyCheck("DC1", "DC1 Latency", 500, Status.Success),
      makeLatencyCheck("DC2", "DC2 Latency", 1000, Status.Warning)
    ),
    takeHighestLevel
  )

  lazy val simpleBoardLayout = Layout(
    Seq(Column(
      33.3,
      Seq(Row("Tier 1", 100, Seq(webserver)))
    ), Column(
      33.3,
      Seq(
        Row("Tier 2 - 1", 50, Seq(gateway)),
        Row("Tier 2 - 2", 50, Seq(pipelineZeta, pipelineOmega))
      )
    ), Column(
      33.3,
      Seq(
        Row("Database", 100, Seq(databaseKappa))
      )
    ))
  )

  lazy val simpleBoard = Board(
    "Example board",
    Seq(webserver, gateway, pipelineZeta, pipelineOmega, databaseKappa),
    takeHighestLevel,
    simpleBoardLayout,
    Seq(webserver -> gateway, gateway -> pipelineZeta, pipelineZeta -> databaseKappa)
  )
}
