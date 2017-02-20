package com.criteo.slab

import com.criteo.slab.core._
import com.criteo.slab.lib.Values.{Latency, Version}

import scala.concurrent.Future

package object example {
  def takeHighestLevel(views: Seq[View]): View = views.sorted.reverse.head

  def makeLatencyCheck(id: String, title: String, value: Long, status: Status) = Check(
    id,
    title,
    () => Future.successful(Latency(value)),
    display = (l: Latency, _: Context) => View(status, s"latency $value")
  )

  def makeVersionCheck(id: String, title: String, value: Int, status: Status) = Check(
    id,
    title,
    () => Future.successful(Version(value)),
    display = (v: Version, _: Context) => View(status, s"version $value")
  )
}
