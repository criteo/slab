package com.criteo.slab.lib

import com.criteo.slab.core.Metrical
import com.criteo.slab.core.Metrical.Type
import org.joda.time.DateTime

object Values {

  // Latency check
  case class Latency(
                      underlying: Long
                    )

  implicit def latencyMetric = new Metrical[Latency] {
    override def toMetrics(value: Latency) = Map("latency" -> value.underlying)

    override def fromMetrics(ms: Metrical.Type): Latency = Latency(ms("latency").toInt)
  }

  // Version check
  case class Version(
                      underlying: Int
                    )

  implicit def versionMetric = new Metrical[Version] {
    override def toMetrics(value: Version): Type = Map("version" -> value.underlying)

    override def fromMetrics(ms: Type): Version = Version(ms("version").toInt)
  }

  // Joda DateTime
  implicit def jodaTimeMetric = new Metrical[DateTime] {
    override def toMetrics(value: DateTime): Type = Map(
      "datetime" -> value.getMillis
    )

    override def fromMetrics(ms: Type): DateTime = new DateTime(ms("datetime"))
  }
}
