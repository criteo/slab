package com.criteo.slab.core

import scala.concurrent.{ExecutionContext, Future}
import org.joda.time.DateTime

case class Check[V: Metrical](
                               id: String,
                               title: String,
                               apply: () => Future[V],
                               display: (V, Context) => View
                             ) {
  def now(implicit store: ValueStore, ec: ExecutionContext): Future[CheckView] = {
    val currCtx = Context(DateTime.now())
    apply()
      .flatMap { value =>
        store.upload(id, implicitly[Metrical[V]].toMetrics(value))
          .map(_ => display(value, currCtx))
      }
      .recover { case e => View(Status.Unknown, e.getMessage) }
      .map(view => CheckView(title, view.status, view.message, view.label))
  }

  def replay(context: Context)(implicit store: ValueStore, ec: ExecutionContext): Future[CheckView] = {
    store
      .fetch(id, context)
      .map(implicitly[Metrical[V]].fromMetrics)
      .map(display(_, context))
      .recover { case e => View(Status.Unknown, e.getMessage) }
      .map(view => CheckView(title, view.status, view.message, view.label))
  }

  def fetchHistory(from: DateTime, until: DateTime)(implicit store: ValueStore, ec: ExecutionContext): Future[Map[Long, CheckView]] = {
    val metrical = implicitly[Metrical[V]]
    store
      .fetchHistory(id, from, until)
      .map {
        _.map { case (timestamp, metrics) =>
          // TODO: handle errors
          // TODO: return a list of (Long, ViewLeaf) ?
          val view = display(metrical.fromMetrics(metrics), Context(new DateTime(timestamp)))
          (timestamp, CheckView(title, view.status, view.message, view.label))
        }
      }
  }
}
