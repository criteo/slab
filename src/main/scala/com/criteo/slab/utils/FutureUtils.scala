package com.criteo.slab.utils

import scala.concurrent.{ExecutionContext, Future}
import com.twitter.util.{Future => TFuture}

import scala.util.{Failure, Success}

object FutureUtils {
  def collect[T](futures: Seq[Future[T]])(implicit ec: ExecutionContext): Future[Seq[T]] =
    futures.foldLeft(Future.successful(Seq.empty[T])) { (fs, f) =>
      fs.flatMap(seq => f.map(_ +: seq))
    }.map(_.reverse)

  implicit class ToTwitterFuture[T](f: Future[T]) {
    import ExecutionContext.Implicits.global
    def toTwitterFuture(): TFuture[T] = {
      val promise = new com.twitter.util.Promise[T]
      f.onComplete {
        case Success(r) => promise.setValue(r)
        case Failure(e) => promise.setException(e)
      }
      promise
    }
  }
}
