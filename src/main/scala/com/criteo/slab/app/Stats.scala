package com.criteo.slab.app

import com.criteo.slab.utils.Jsonable

case class Stats(
                  successes: Int,
                  warnings: Int,
                  errors: Int,
                  total: Int
                )

object Stats {
  implicit object ToJSON extends Jsonable[Stats]
}

