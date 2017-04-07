package com.criteo.slab

import com.criteo.slab.core._
import com.criteo.slab.lib.Values.{Latency, Version}

import scala.concurrent.Future
import scala.util.Random

package object example {
  // take the most critical except unknown ones
  def takeMostCritical(views: Seq[View]): View = views.filter(_.status != Status.Unknown).sorted.reverse.head

  def makeRandomLatencyCheck(id: String, title: String, label: Option[String] = None) = Check(
    id,
    title,
    () => Future.successful(
      Latency(Random.nextInt(1000))
    ),
    display = (l: Latency, _: Context) => {
      val status = if (l.underlying >= 990) {
        Status.Error
      } else if (l.underlying >= 980) {
        Status.Warning
      } else {
        Status.Success
      }
      View(status, s"latency ${l.underlying}ms", label)
    }
  )

  def makeVersionCheck(id: String, title: String, value: Int, status: Status, label: Option[String] = None) = Check(
    id,
    title,
    () => Future.successful(Version(value)),
    display = (_: Version, _: Context) => View(status, s"version $value", label)
  )
}
