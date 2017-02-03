package com.criteo.slab.core

import com.twitter.util.Future

case class Board(
                  title: String,
                  groups: Seq[Box],
                  aggregate: (Seq[View]) => View
                ) {
  def apply(context: Option[Context])(implicit valueStore: ValueStore): Future[ViewTree] =
    Future.collect(
      groups.map(_.apply(context))
    ).map(viewNodes =>
      ViewNode(
        title,
        aggregate(viewNodes.map(_.view)),
        viewNodes
      )
    )
}
