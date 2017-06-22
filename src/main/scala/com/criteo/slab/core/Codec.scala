package com.criteo.slab.core

import scala.util.Try

/**
  * Codec for values to be put in a store
  * @tparam T Input type
  * @tparam Repr Encoded type
  */
trait Codec[T, Repr] {
  def encode(v: T): Repr

  def decode(v: Repr): Try[T]
}

