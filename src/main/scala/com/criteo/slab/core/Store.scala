package com.criteo.slab.core

import scala.concurrent.Future

trait Store[S[_] <: Storable[_]] {
  def upload[T: S](id: String, value: T): Future[Unit]
  def fetch[T: S](id: String, context: Context): Future[T]
}

trait Storable[In] {
  type Out
  def store(in: In): Out
  def restore(out: Out): In
}

trait GraphiteMetrical[T] extends Storable[T] {
  type Out = Seq[(String, Double)]
}

class GraphiteStore(url: String) extends Store[GraphiteMetrical] {
  def upload[T: GraphiteMetrical](id: String, value: T): Future[Unit] = Future.successful(())
  def fetch[T: GraphiteMetrical](id: String, context: Context): Future[T] =
    Future.successful(implicitly[GraphiteMetrical[T]].restore(Seq.empty))
}
