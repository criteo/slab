package com.criteo.slab.core

import java.time.Instant

import com.criteo.slab.utils.FutureUtils

import scala.concurrent.{ExecutionContext, Future}

/** Top level component
  *
  * @param title The board title
  * @param boxes The children boxes
  * @param aggregate Aggregates its children boxes views
  * @param layout The layout of the board
  * @param links Defines links between boxes, will draw lines in the UI
  * @param valueStore The store used for
  */
case class Board(
                  title: String,
                  boxes: Seq[Box],
                  aggregate: Seq[View] => View,
                  layout: Layout,
                  links: Seq[(Box, Box)] = Seq.empty
                )(implicit valueStore: ValueStore) {
  require({
    val boxesInBoard = boxes.toSet
    val boxesInLayout = layout.columns.foldLeft(Set.empty[Box]) { (set, col) =>
      set ++ col.rows.foldLeft(Set.empty[Box]) { (set, row) => set ++ row.boxes.toSet }
    }
    val intersect = boxesInBoard.intersect(boxesInLayout)
    intersect.size == boxesInBoard.size && intersect.size == boxesInLayout.size
  }, "Board definition error, please make sure all boxes are present both in board and layout")

  def apply(context: Option[Context])(implicit ec: ExecutionContext): Future[BoardView] =
    FutureUtils.join(
      boxes.map(_.apply(context))
    ).map { boxViews =>
      val view = aggregate(boxViews.map(_.asView))
      BoardView(
        title,
        view.status,
        view.message,
        boxViews
      )
    }

  def fetchHistory(from: Instant, until: Instant)(implicit ec: ExecutionContext): Future[Map[Long, BoardView]] = {
    boxes.map(_.fetchHistory(from, until))
    FutureUtils.join(
      boxes.map(_.fetchHistory(from, until))
    ).map { maps =>
      maps.flatMap(_.toList)
        .groupBy(_._1)
        .mapValues { in =>
          val boxViews = in.map(_._2)
          val view = aggregate(boxViews.map(_.asView))
          BoardView(
            title,
            view.status,
            view.message,
            boxViews
          )
        }
    }
  }
}
