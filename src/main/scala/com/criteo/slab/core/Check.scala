package com.criteo.slab.core

import scala.concurrent.{ExecutionContext, Future}
import org.joda.time.DateTime

case class Check[V: Metrical](
                               id: String,
                               title: String,
                               apply: () => Future[V],
                               display: (V, Context) => View
                             ) {
  def now(implicit store: ValueStore, ec: ExecutionContext): Future[ViewLeaf] = {
    val currCtx = Context(DateTime.now())
    apply()
      .flatMap { value =>
        store.upload(id, implicitly[Metrical[V]].toMetrics(value))
          .map(_ => display(value, currCtx))
      }
      .recover { case e => View(Status.Unknown, e.getMessage) }
      .map(ViewLeaf(title, _))
  }

  def replay(context: Context)(implicit store: ValueStore, ec: ExecutionContext): Future[ViewLeaf] = {
    store
      .fetch(id, context)
      .map(implicitly[Metrical[V]].fromMetrics)
      .map(display(_, context))
      .recover { case e => View(Status.Unknown, e.getMessage) }
      .map(ViewLeaf(title, _))
  }
}
