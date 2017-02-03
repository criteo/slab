package com.criteo.slab.helper

import com.twitter.util.{Return, Throw}
import org.scalatest.concurrent.{Futures, ScalaFutures}

import scala.concurrent.{Future, Promise}

trait TwitterFutures extends Futures with ScalaFutures {

  implicit class FutureConceptConverter[T](f: com.twitter.util.Future[T]) {
    def toFutureConcept: FutureConcept[T] = new FutureConcept[T] {
      override def eitherValue: Option[Either[Throwable, T]] = {
        f.poll.map {
          case Return(o) => Right(o)
          case Throw(e) => Left(e)
        }
      }

      override def isCanceled: Boolean = false

      override def isExpired: Boolean = false
    }

    def toScala: Future[T] = {
      val promise = Promise[T]()
      f.onSuccess { r: T => promise.success(r) }
      f.onFailure { e: Throwable => promise.failure(e) }
      promise.future
    }
  }
}

object TwitterFutures extends TwitterFutures