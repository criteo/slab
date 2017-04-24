package com.criteo.slab.core

/** Defines types that are serializable to metrical values, and deserializable from metrical values
  *
  * @tparam T The underlying type that implements [[Metrical]] interfaces
  */
trait Metrical[T] {
  def toMetrics(value: T): Metrical.Out

  def fromMetrics(ms: Metrical.Out): T
}

object Metrical {
  type Out = Map[String, Double]
}
