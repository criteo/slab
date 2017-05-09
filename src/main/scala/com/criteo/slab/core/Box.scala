package com.criteo.slab.core

import java.time.Instant

import com.criteo.slab.utils.Jsonable
import org.json4s.CustomSerializer
import org.json4s.JsonDSL._

import scala.concurrent.{ExecutionContext, Future}

/** A box that groups checks
  *
  * @param title       The title
  * @param checks      The checks
  * @param aggregate   Aggregates the views of its checks, return a view
  * @param description The description of the box in markdown syntax
  * @param labelLimit  The limit of visible check labels shown on the box
  */
case class Box(
                title: String,
                checks: Seq[Check[_]],
                aggregate: (Seq[View]) => View,
                description: Option[String] = None,
                labelLimit: Option[Int] = None
              ) {
  def apply(context: Option[Context])(implicit valueStore: ValueStore, ec: ExecutionContext): Future[BoxView] = {
    Future
      .sequence(checks.map(c => context.fold(c.now)(c.replay)))
      .map { checkViews =>
        val view = aggregate(checkViews.map(_.asView))
        BoxView(
          title,
          view.status,
          view.message,
          checkViews
        )
      }
  }

  def fetchHistory(from: Instant, until: Instant)(implicit store: ValueStore, ec: ExecutionContext): Future[Map[Long, BoxView]] = {
    Future
      .sequence(checks.map(_.fetchHistory(from, until)))
      .map { maps =>
        maps.flatMap(_.toList)
          .groupBy(_._1)
          .mapValues { in =>
            val checkViews = in.map(_._2)
            val view = aggregate(checkViews.map(_.asView))
            BoxView(
              title,
              view.status,
              view.message,
              checkViews
            )
          }
      }
  }
}

object Box {

  implicit object toJSON extends Jsonable[Box] {
    override val serializers = List(Ser)

    object Ser extends CustomSerializer[Box](_ => ( {
      case _ => throw new NotImplementedError("Not deserializable")
    }, {
      case box: Box =>
        ("title" -> box.title) ~ ("description" -> box.description) ~ ("labelLimit" -> box.labelLimit.getOrElse(box.checks.size))
    }
    ))

  }

}
