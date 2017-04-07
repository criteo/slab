package com.criteo.slab.core

trait Metrical[T] {
  def toMetrics(value: T): Metrical.Type

  def fromMetrics(ms: Metrical.Type): T
}

object Metrical {
  type Type = Map[String, Double]
}
