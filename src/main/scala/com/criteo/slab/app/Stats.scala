package com.criteo.slab.app

import com.criteo.slab.utils.Jsonable

/** Statistics of history
  *
  * @param successes Number of checks that are successful
  * @param warnings  Number of checks that are in the warning state
  * @param errors    Number of checks that are errors
  * @param unknown   Number of checks that are unknown
  * @param total     Total number of checks
  */
case class Stats(
                  successes: Int,
                  warnings: Int,
                  errors: Int,
                  unknown: Int,
                  total: Int
                )

object Stats {

  implicit object ToJSON extends Jsonable[Stats]

}
