package com.criteo.slab.core

trait Metrical[T] {
  def toMetrics(value: T): Metrical.Type

  def fromMetrics(ms: Metrical.Type): T

  // TODO: define a chart type
  def toChartable(value: T): Double = 0
}

object Metrical {
  type Type = Map[String, Double]
}
