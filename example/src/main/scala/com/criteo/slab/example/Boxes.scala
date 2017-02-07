package com.criteo.slab.example

import com.criteo.slab.core.{Box, Status}

object Boxes {

  import Checks._

  lazy val webserver = Box(
    "Webserver Alpha",
    Seq(
      versionCheck, latencyCheck
    ),
    takeHighestLevel
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
}
