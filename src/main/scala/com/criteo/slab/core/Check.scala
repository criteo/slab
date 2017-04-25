package com.criteo.slab.core

import java.time.Instant

import scala.concurrent.{ExecutionContext, Future}

case class Check[V: Metrical](
                               id: String,
                               title: String,
                               apply: () => Future[V],
                               display: (V, Context) => View
                             ) {
  def now(implicit store: ValueStore, ec: ExecutionContext): Future[CheckView] = {
    val currCtx = Context(Instant.now())
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

  def fetchHistory(from: Instant, until: Instant)(implicit store: ValueStore, ec: ExecutionContext): Future[Map[Long, CheckView]] = {
    val metrical = implicitly[Metrical[V]]
    store
      .fetchHistory(id, from, until)
      .map {
        _.map { case (timestamp, metrics) =>
          val view = display(metrical.fromMetrics(metrics), Context(Instant.ofEpochMilli(timestamp)))
          (timestamp, CheckView(title, view.status, view.message, view.label))
        }
      }
  }
}
