package com.criteo.slab.example

import com.criteo.slab.core._

import com.criteo.slab.lib.GraphiteStore
import scala.concurrent.ExecutionContext.Implicits.global

object SimpleBoard {

  lazy val webserver = Box(
    "Webserver Alpha",
    Seq(
      makeVersionCheck("web.version", "Version", 100, Status.Success, Some("V100")),
      makeLatencyCheck("web.latency", "Latency", 300, Status.Success)
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
      makeLatencyCheck("EU", "EU Gateway latency", 200, Status.Success),
      makeLatencyCheck("US", "US Gateway latency", 300, Status.Success)
    ),
    takeMostCritical
  )

  lazy val pipelineZeta = Box(
    "Pipeline Zeta",
    Seq(
      makeLatencyCheck("A", "Job A latency", 1000, Status.Success),
      makeLatencyCheck("B", "Job B latency", 2000, Status.Warning),
      makeLatencyCheck("C", "Job C latency", 3000, Status.Error)
    ),
    takeMostCritical
  )

  lazy val pipelineOmega = Box(
    "Pipeline Omega",
    Seq(
      makeLatencyCheck("A", "Job A latency", 1000, Status.Unknown),
      makeLatencyCheck("B", "Job B latency", 1000, Status.Success),
      makeLatencyCheck("C", "Job C latency", 1000, Status.Success),
      makeLatencyCheck("D", "Job D latency", 1000, Status.Success),
      makeLatencyCheck("E", "Job E latency", 1000, Status.Success)
    ),
    takeMostCritical,
    labelLimit = Some(3)
  )

  lazy val databaseKappa = Box(
    "Database Kappa",
    Seq(
      makeLatencyCheck("DC1", "DC1 Latency", 500, Status.Success),
      makeLatencyCheck("DC2", "DC2 Latency", 1000, Status.Warning)
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
