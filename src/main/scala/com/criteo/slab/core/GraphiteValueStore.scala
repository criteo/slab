package com.criteo.slab.core

import com.twitter.util.Future

class GraphiteValueStore extends ValueStore {
  override def upload(id: String, values: Seq[(String, Long)]): Future[Unit] = ???

  override def fetch(id: String, context: Context): Future[Seq[(String, Long)]] = ???
}
