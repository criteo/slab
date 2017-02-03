package com.criteo.slab.utils

import scala.util.Try

trait Jsonable[T] {
  def parse(in: String): Try[T]
  def serialize(in: T): String
}

object Jsonable {
  implicit class ToJSON[T: Jsonable](in: T) {
    def toJSON: String = implicitly[Jsonable[T]].serialize(in)
  }
  implicit class FromJSON(in: String) {
    def parseTo[T: Jsonable]: Try[T] = implicitly[Jsonable[T]].parse(in)
  }
}
