package com.criteo.slab.core

import java.time.Instant

import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

/** A class for declaring a metric to check
  *
  * @param id The identifier
  * @param title The title of the check
  * @param apply A function when called, should return a future of target value
  * @param display A function that takes a checked value and a [[com.criteo.slab.core.Context Context]]
  * @tparam V A type parameter which corresponds to the type of the checked value
  */
case class Check[V: Metrical](
                               id: String,
                               title: String,
                               apply: () => Future[V],
                               display: (V, Context) => View
                             ) {
  private val logger = LoggerFactory.getLogger(this.getClass)

  /** Execute a check
    *
    * @param store The store for uploading the checked value
    * @param ec The execution context
    * @return Future of a [[CheckView]]
    */
  def now(implicit store: ValueStore, ec: ExecutionContext): Future[CheckView] = {
    val currCtx = Context(Instant.now())
    apply()
      .flatMap { value =>
        store.upload(id, implicitly[Metrical[V]].toMetrics(value))
          .map(_ => display(value, currCtx))
      }
      .recover { case e =>
        logger.error(e.getMessage, e)
        View(Status.Unknown, e.getMessage)
      }
      .map(view => CheckView(title, view.status, view.message, view.label))
  }

  /** Replay the check at the given datetime
    *
    * @param context The context
    * @param store The store for fetching the data
    * @param ec The execution context
    * @return Future of a [[CheckView]]
    */
  def replay(context: Context)(implicit store: ValueStore, ec: ExecutionContext): Future[CheckView] = {
    store
      .fetch(id, context)
      .map(implicitly[Metrical[V]].fromMetrics)
      .map(display(_, context))
      .recover { case e =>
        logger.error(e.getMessage, e)
        View(Status.Unknown, e.getMessage)
      }
      .map(view => CheckView(title, view.status, view.message, view.label))
  }

  /** Fetch the history within the given date range
    *
    * @param from The start date
    * @param until The end date
    * @param store The store for fetching the data
    * @param ec The execution context
    * @return Future of a [[CheckView]]
    */
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
