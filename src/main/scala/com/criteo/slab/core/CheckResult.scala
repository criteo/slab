package com.criteo.slab.core

/**
  * Represents the result of a check
  *
  * @param view  The view of the check
  * @param value The value of the check
  * @tparam T The checked value type
  */
case class CheckResult[T](
                           view: View,
                           value: Option[T]
                         )
