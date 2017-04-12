package com.criteo.slab

import com.criteo.slab.core.Metrical.Out
import com.criteo.slab.lib.Values.{Latency, Version}
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

package object core {

  // Value stores
  object TestStore extends ValueStore {
    override def fetch(id: String, context: Context) = {
      id match {
        case "app.version" =>
          Future.successful(Map("version" -> context.when.getMillis))
        case "app.latency" =>
          Future.successful(Map("latency" -> context.when.getMillis))
        case _ =>
          Future.failed(new Exception("network error"))
      }
    }

    override def upload(id: String, values: Metrical.Out) = Future.successful(())

    override def fetchHistory(id: String, from: DateTime, until: DateTime): Future[Map[Long, Out]] = Future.successful(Map.empty)
  }

  val versionCheck = Check(
    "app.version",
    "app version",
    () => Future.successful(Version(9000)),
    display = (v: Version, context: Context) => View(Status.Success, s"version ${v.underlying}")
  )

  val failedVersionCheck = Check[Version](
    "app.version",
    "app version",
    () => Future.failed(new Exception("failed check")),
    display = (v: Version, context: Context) => View(Status.Success, s"version ${v.underlying}")
  )

  val latencyCheck = Check(
    "app.latency",
    "app latency",
    () => Future.successful(Latency(2000)),
    display = (l: Latency, context: Context) => View(Status.Warning, s"latency ${l.underlying}")
  )
}
