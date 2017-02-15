package com.criteo.slab.core

import com.twitter.util.Future

case class Board(
                  title: String,
                  boxes: Seq[Box],
                  aggregate: Seq[View] => View,
                  layout: Layout,
                  links: Seq[(Box, Box)] = Seq.empty
                ) {
  require({
    val boxesInBoard = boxes.toSet
    val boxesInLayout = layout.columns.foldLeft(Set.empty[Box]) { (set, col) =>
      set ++ col.rows.foldLeft(Set.empty[Box]) { (set, row) => set ++ row.boxes.toSet }
    }
    val intersect = boxesInBoard.intersect(boxesInLayout)
    intersect.size == boxesInBoard.size && intersect.size == boxesInLayout.size
  }, "Board definition error, please make sure all boxes are present both in board and layout")

  def apply(context: Option[Context])(implicit valueStore: ValueStore): Future[ViewTree] =
    Future.collect(
      boxes.map(_.apply(context))
    ).map(viewNodes =>
      ViewNode(
        title,
        aggregate(viewNodes.map(_.view)),
        viewNodes
      )
    )
}
