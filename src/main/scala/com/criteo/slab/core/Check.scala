package com.criteo.slab.core

import scala.concurrent.Future

/** Basic unit for declaring a metric to check
  *
  * @param id      The identifier
  * @param title   The title of the check
  * @param apply   A function when called, should return a future of target value
  * @param display A function that takes a checked value and a [[com.criteo.slab.core.Context Context]]
  * @tparam T A type parameter which corresponds to the type of the checked value
  */
case class Check[T](
                     id: String,
                     title: String,
                     apply: () => Future[T],
                     display: (T, Context) => View
                   )
