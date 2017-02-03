package com.criteo.slab.core

trait Metrical[T] {
  def toMetrics(value: T): Seq[(String, Long)]
  def fromMetrics(ms: Seq[(String, Long)]): T
}
