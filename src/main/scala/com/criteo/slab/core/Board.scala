package com.criteo.slab.core

import com.criteo.slab.utils.FutureUtils
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

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
    ).map{boxViews =>
      val view = aggregate(boxViews.map(_.asView))
      BoardView(
        title,
        view.status,
        view.message,
        boxViews
      )
    }

  def fetchHistory(from: DateTime, until: DateTime)(implicit ec: ExecutionContext): Future[Map[Long, BoardView]] = {
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
