package com.criteo.slab.core

/** A view represents the status of a given check
  *
  * @param status The status of the underlying check
  * @param message The message to show
  * @param label The label
  */
case class View(
                 status: Status,
                 message: String,
                 label: Option[String] = None
               )

object View {
  implicit object DefaultOrd extends Ordering[View] {
    override def compare(x: View, y: View): Int = x.status.level - y.status.level
  }
}