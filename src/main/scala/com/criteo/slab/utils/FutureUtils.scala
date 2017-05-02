package com.criteo.slab.utils

import scala.concurrent.{ExecutionContext, Future}

/**
  * Utilities for Scala futures
  */
private[slab] object FutureUtils {
  /**
    *
    * @param futures
    * @param ec The execution context
    * @tparam T The type of the wrapped value
    * @return A future of a sequence of T
    */
  def join[T](futures: Seq[Future[T]])(implicit ec: ExecutionContext): Future[Seq[T]] =
    futures.foldLeft(Future.successful(Seq.empty[T])) { (fs, f) =>
      fs.flatMap(seq => f.map(_ +: seq))
    }.map(_.reverse)
}
