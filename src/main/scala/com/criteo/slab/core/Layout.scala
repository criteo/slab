package com.criteo.slab.core

import com.criteo.slab.utils.Jsonable
import org.json4s.Serializer

/** Represents a column on a board
  *
  * @param percentage The percentage it takes as width
  * @param rows The rows in the column
  */
case class Column(percentage: Double, rows: Row*)

/** Represents a row inside a column
  *
  * @param title The title
  * @param percentage The percentage it takes as height, defaults to 100
  * @param boxes The boxes to be displayed in the row
  */
case class Row(title: String, percentage: Double = 100, boxes: Seq[Box[_]])

/** Defines the layout of a board
  *
  * @param columns List of [[Column]]
  */
case class Layout(columns: Column*)

object Layout {

  implicit object ToJSON extends Jsonable[Layout] {
    override val serializers: Seq[Serializer[_]] = implicitly[Jsonable[Box[_]]].serializers
  }
}
