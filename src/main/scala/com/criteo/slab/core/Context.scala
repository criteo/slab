package com.criteo.slab.core

import java.time.Instant

/** Represents the context when a check occurs
  *
  * @param when The datetime when it occurs
  */
case class Context(
                    when: Instant
                  )

object Context {
  def now = Context(Instant.now)
}