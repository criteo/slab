package com.criteo.slab.utils

import scala.concurrent.{ExecutionContext, Future}

object FutureUtils {
  def collect[T](futures: Seq[Future[T]])(implicit ec: ExecutionContext): Future[Seq[T]] =
    futures.foldLeft(Future.successful(Seq.empty[T])) { (fs, f) =>
      fs.flatMap(seq => f.map(_ +: seq))
    }.map(_.reverse)
}
