package com.criteo.slab.core

import org.slf4j.LoggerFactory
import shapeless.ops.hlist.ToTraversable
import shapeless.{HList, UnaryTCConstraint}

/** Top level component
  *
  * @param title     The board title
  * @param boxes     The children boxes
  * @param aggregate Aggregates its children boxes views
  * @param layout    The layout of the board
  * @param links     Defines links between boxes, will draw lines in the UI
  * @param slo       Defines the SLO threshold for the calendar.
  */
case class Board[B <: HList](
                              title: String,
                              boxes: B,
                              aggregate: (Map[Box[_], View], Context) => View,
                              layout: Layout,
                              links: Seq[(Box[_], Box[_])] = Seq.empty,
                              slo: Double = 0.97
                            )(
                              implicit
                              constraint: UnaryTCConstraint[B, Box],
                              boxSet: ToTraversable.Aux[B, Set, Box[_]]
                            ) {
  private val logger = LoggerFactory.getLogger(this.getClass)
  require({
    val boxesInBoard = boxes.to(boxSet)
    val boxesInLayout = layout.columns.foldLeft(Set.empty[Box[_]]) { (set, col) =>
      set ++ col.rows.foldLeft(Set.empty[Box[_]]) { (set, row) => set ++ row.boxes.toSet }
    }
    val notInLayout = boxesInBoard.diff(boxesInLayout).map(_.title)
    if (notInLayout.size > 0)
      logger.error(s"Boxes not present in the layout but in the 'boxes' field: ${notInLayout.mkString(", ")}")
    val notInBoard = boxesInLayout.diff(boxesInBoard).map(_.title)
    if (notInBoard.size > 0)
      logger.error(s"Boxes not present in the 'boxes' field but in the layout: ${notInBoard.mkString(", ")}")
    notInLayout.size == 0 && notInBoard.size == 0
  }, "Board definition error, please make sure all boxes are present both in board and layout")

}
