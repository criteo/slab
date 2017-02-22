package com.criteo.slab.lib

import com.criteo.slab.core.Metrical
import org.joda.time.DateTime

object Values {

  // Latency check
  case class Latency(
                      underlying: Long
                    )

  implicit def latencyMetric = new Metrical[Latency] {
    override def toMetrics(value: Latency): Seq[(String, Long)] = Seq(("latency", value.underlying))

    override def fromMetrics(ms: Seq[(String, Long)]): Latency = Latency(ms.head._2)
  }


  // Version check
  case class Version(
                      underlying: Int
                    )

  implicit def versionMetric = new Metrical[Version] {
    override def toMetrics(value: Version): Seq[(String, Long)] = Seq(("version", value.underlying))

    override def fromMetrics(ms: Seq[(String, Long)]): Version = Version(ms.head._2.toInt)
  }

  case class Datetime(
                       underlying: DateTime
                     )

  implicit def dateTimeMetric = new Metrical[Datetime] {
    override def toMetrics(value: Datetime): Seq[(String, Long)] = Seq(("datetime", value.underlying.getMillis))

    override def fromMetrics(ms: Seq[(String, Long)]): Datetime = Datetime(new DateTime(ms.head._2))
  }
}
