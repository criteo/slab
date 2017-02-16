package com.criteo.slab.core

import com.criteo.slab.utils.Jsonable
import com.twitter.util.Future
import org.json4s.CustomSerializer
import org.json4s.JsonAST.JString

case class Box(
                title: String,
                checks: Seq[Check[_]],
                aggregate: (Seq[View]) => View,
                description: Option[String] = None
              ) {
  def apply(context: Option[Context])(implicit valueStore: ValueStore): Future[ViewNode] = {
    Future
      .collect(checks.map(c => context.fold(c.now)(c.replay)))
      .map(viewLeaves =>
        ViewNode(
          title,
          aggregate(viewLeaves.map(_.view)),
          viewLeaves,
          description
        )
      )
  }
}

object Box {
  implicit object toJSON extends Jsonable[Box] {
    override val serializers = List(Ser)

    object Ser extends CustomSerializer[Box](_ => (
      {
        case _ => throw new NotImplementedError("Not deserializable")
      },
      {
        case box: Box => JString(box.title)
      }
    ))
  }
}
