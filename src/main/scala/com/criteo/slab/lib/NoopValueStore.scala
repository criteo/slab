package com.criteo.slab.lib

import java.time.Instant

import com.criteo.slab.core.{Context, Metrical, ValueStore}

import scala.concurrent.Future

/** A dummy store that does nothing
  *
  */
object NoopValueStore extends ValueStore {
  override def upload(id: String, values: Map[String, Double]): Future[Unit] = Future.successful(())

  override def fetch(id: String, context: Context): Future[Map[String, Double]] = Future.successful(Map.empty)

  override def fetchHistory(id: String, from: Instant, until: Instant): Future[Map[Long, Metrical.Out]] = Future.successful(Map.empty)
}
