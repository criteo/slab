package com.criteo.slab.lib

import java.time.Instant

import com.criteo.slab.core.Metrical
import com.criteo.slab.core.Metrical.Out

object Values {

  // Latency check
  case class Latency(
                      underlying: Long
                    )

  implicit def latencyMetric = new Metrical[Latency] {
    override def toMetrics(value: Latency) = Map("latency" -> value.underlying)

    override def fromMetrics(ms: Metrical.Out): Latency = Latency(ms("latency").toInt)
  }

  // Version check
  case class Version(
                      underlying: Double
                    )

  implicit def versionMetric = new Metrical[Version] {
    override def toMetrics(value: Version): Out = Map("version" -> value.underlying)

    override def fromMetrics(ms: Out): Version = Version(ms("version"))
  }

  // Instant
  implicit def instantMetric = new Metrical[Instant] {
    override def toMetrics(value: Instant): Out = Map(
      "datetime" -> value.toEpochMilli
    )

    override def fromMetrics(ms: Out): Instant = Instant.ofEpochMilli(ms("datetime").toLong)
  }
}
