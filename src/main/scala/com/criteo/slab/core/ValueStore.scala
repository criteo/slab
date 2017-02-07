package com.criteo.slab.core

import com.twitter.util.Future

trait ValueStore {
  def upload(id: String, values: Seq[(String, Long)]): Future[Unit]
  def fetch(id: String, context: Context): Future[Seq[(String, Long)]]
}

object NoopValueStore extends ValueStore {
  override def upload(id: String, values: Seq[(String, Long)]): Future[Unit] = Future.value(())

  override def fetch(id: String, context: Context): Future[Seq[(String, Long)]] = Future.value(Seq.empty)
}
