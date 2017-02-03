package com.criteo.slab.core

import com.twitter.util.Future

case class Box(
                title: String,
                checks: Seq[Check[_]],
                aggregate: (Seq[View]) => View
              ) {
  def apply(context: Option[Context])(implicit valueStore: ValueStore): Future[ViewNode] = {
    Future
      .collect(checks.map(c => context.fold(c.now)(c.replay)))
      .map(viewLeaves =>
        ViewNode(
          title,
          aggregate(viewLeaves.map(_.view)),
          viewLeaves
        )
      )
  }
}
