package com.criteo.slab.core

trait Metrical[T] {
  def toMetrics(value: T): Metrical.Out

  def fromMetrics(ms: Metrical.Out): T
}

object Metrical {
  type Out = Map[String, Double]
}
