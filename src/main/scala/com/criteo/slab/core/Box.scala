package com.criteo.slab.core

import com.criteo.slab.utils.{FutureUtils, Jsonable}
import org.joda.time.DateTime
import org.json4s.CustomSerializer
import org.json4s.JsonAST.JString

import scala.concurrent.{ExecutionContext, Future}

case class Box(
                title: String,
                checks: Seq[Check[_]],
                aggregate: (Seq[View]) => View,
                description: Option[String] = None,
                labelLimit: Option[Int] = None
              ) {
  def apply(context: Option[Context])(implicit valueStore: ValueStore, ec: ExecutionContext): Future[ViewNode] = {
    FutureUtils
      .join(checks.map(c => context.fold(c.now)(c.replay)))
      .map(viewLeaves =>
        ViewNode(
          title,
          aggregate(viewLeaves.map(_.view)),
          viewLeaves,
          description,
          labelLimit
        )
      )
  }

  def fetchTimeSeries(from: DateTime, until: DateTime)(implicit store: ValueStore, ec: ExecutionContext): Future[Seq[TimeSeries]] = {
    FutureUtils.join(
      checks.map { c =>
        c.fetchTimeSeries(from, until)
          .map(TimeSeries(c.title, _))
      }
    )
  }

  def fetchHistory(from: DateTime, until: DateTime)(implicit store: ValueStore, ec: ExecutionContext): Future[Map[Long, ViewNode]] = {
    FutureUtils.join(
      checks.map(_.fetchHistory(from, until))
    ).map { maps =>
      maps.flatMap(_.toList)
        .groupBy(_._1)
        .mapValues { in =>
          val viewLeafs = in.map(_._2)
          ViewNode(
            title,
            aggregate(viewLeafs.map(_.view)),
            viewLeafs,
            description,
            labelLimit
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
      case box: Box => JString(box.title)
    }
    ))

  }

}
