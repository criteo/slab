package com.criteo.slab

import com.criteo.slab.core._
import com.twitter.util.Future

package object example {
  def takeHighestLevel(views: Seq[View]): View = views.sorted.reverse.head

  // Version check
  case class Version(
                      underlying: Int
                    )

  implicit def versionMetric = new Metrical[Version] {
    override def toMetrics(value: Version): Seq[(String, Long)] = Seq(("version", value.underlying))

    override def fromMetrics(ms: Seq[(String, Long)]): Version = Version(ms.head._2.toInt)
  }

  // Latency check
  case class Latency(
                      underlying: Long
                    )

  implicit def latencyMetric = new Metrical[Latency] {
    override def toMetrics(value: Latency): Seq[(String, Long)] = Seq(("latency", value.underlying))

    override def fromMetrics(ms: Seq[(String, Long)]): Latency = Latency(ms.head._2)
  }

  def makeLatencyCheck(id: String, title: String, value: Long, status: Status) = Check(
    id,
    title,
    () => Future.value(Latency(value)),
    display = (l: Latency, _: Context) => View(status, s"latency $value")
  )

  def makeVersionCheck(id: String, title: String, value: Int, status: Status) = Check(
    id,
    title,
    () => Future.value(Version(value)),
    display = (v: Version, _: Context) => View(status, s"version $value")
  )
}
