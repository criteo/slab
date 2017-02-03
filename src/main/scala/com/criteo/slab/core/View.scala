package com.criteo.slab.core

case class View(
                 status: Status,
                 message: String
               )

object View {
  implicit object DefaultOrd extends Ordering[View] {
    override def compare(x: View, y: View): Int = x.status.level - y.status.level
  }
}