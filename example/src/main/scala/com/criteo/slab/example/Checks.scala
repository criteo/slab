package com.criteo.slab.example

import com.criteo.slab.core._
import com.twitter.util.Future

object Checks {

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

  lazy val versionCheck = Check(
    "app.version",
    "app version",
    () => HttpClient("/echo/1000").map(res => Version(res.contentString.toInt)),
    display = (v: Version, context: Context) => View(Status.Success, s"version ${v.underlying}")
  )

  lazy val latencyCheck = Check(
    "app.latency",
    "app latency",
    () => HttpClient("/echo/2000").map(res => Latency(res.contentString.toInt)),
    display = (l: Latency, _: Context) => View(Status.Warning, s"latency ${l.underlying}")
  )

  def makeLatencyCheck(id: String, title: String, value: Long, status: Status) = Check(
    id,
    title,
    () => Future.value(Latency(value)),
    display = (l: Latency, _: Context) => View(status, s"latency $value")
  )
}
