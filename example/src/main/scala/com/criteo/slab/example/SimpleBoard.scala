package com.criteo.slab.example

import com.criteo.slab.core._

object SimpleBoard {

  lazy val webserver = Box(
    "Webserver Alpha",
    Seq(
      makeVersionCheck("web.version", "Version", 1.2, Status.Success, Some("V1.2")),
      makeRandomLatencyCheck("web.latency", "Latency")
    ),
    takeMostCritical,
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
      makeRandomLatencyCheck("EU", "EU Gateway latency"),
      makeRandomLatencyCheck("US", "US Gateway latency")
    ),
    takeMostCritical
  )

  lazy val pipelineZeta = Box(
    "Pipeline Zeta",
    Seq(
      makeRandomLatencyCheck("A", "Job A latency"),
      makeRandomLatencyCheck("B", "Job B latency"),
      makeRandomLatencyCheck("C", "Job C latency")
    ),
    takeMostCritical
  )

  lazy val pipelineOmega = Box(
    "Pipeline Omega",
    Seq(
      makeRandomLatencyCheck("A", "Job A latency"),
      makeRandomLatencyCheck("B", "Job B latency"),
      makeRandomLatencyCheck("C", "Job C latency"),
      makeRandomLatencyCheck("D", "Job D latency"),
      makeRandomLatencyCheck("E", "Job E latency")
    ),
    takeMostCritical,
    labelLimit = Some(3)
  )

  lazy val databaseKappa = Box(
    "Database Kappa",
    Seq(
      makeRandomLatencyCheck("DC1", "DC1 Latency"),
      makeRandomLatencyCheck("DC2", "DC2 Latency")
    ),
    takeMostCritical
  )

  lazy val ui = Box(
    "User interface",
    Seq(
      makeVersionCheck("ui.version", "Version 1000", 1000, Status.Success)
    ),
    takeMostCritical,
    labelLimit = Some(0)
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
        Row("Tier 3", 100, Seq(databaseKappa, ui))
      )
    ))
  )

  def apply()(implicit valueStore: ValueStore) = Board(
    "Simple board",
    Seq(webserver, gateway, pipelineZeta, pipelineOmega, databaseKappa, ui),
    takeMostCritical,
    simpleBoardLayout,
    Seq(webserver -> gateway, gateway -> pipelineZeta, pipelineZeta -> databaseKappa)
  )
}
