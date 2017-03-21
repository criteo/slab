package com.criteo.slab


import scala.util.{Failure, Success, Try}

package object utils {
  def collectTries[T](tries: Seq[Try[T]]): Try[Seq[T]] = {
    tries.foldLeft(Success(Seq.empty[T]): Try[Seq[T]])( (result, t) =>
      t match {
        case Failure(e) => Failure(e)
        case Success(v) => result.map(v +: _)
      }
    ).map(_.reverse)
  }
}
