package com.criteo.slab.core

import java.time.Instant

import scala.concurrent.Future

/** Persists checked values
  *
  * @tparam Repr Persistent data type in the store
  */
trait Store[Repr] {
  def upload[T](id: String, context: Context, v: T)(implicit codec: Codec[T, Repr]): Future[Unit]

  def fetch[T](id: String, context: Context)(implicit codec: Codec[T, Repr]): Future[T]

  def fetchHistory[T](id: String, from: Instant, until: Instant)(implicit codec: Codec[T, Repr]): Future[Seq[(Long, T)]]
}
