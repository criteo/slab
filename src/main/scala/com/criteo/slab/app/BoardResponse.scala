package com.criteo.slab.app

import com.criteo.slab.core.{Box, Layout, ViewTree}
import com.criteo.slab.utils.Jsonable
import org.json4s.JsonAST.{JArray, JString}
import org.json4s.{CustomSerializer, Serializer}

case class BoardResponse(
                          view: ViewTree,
                          layout: Layout,
                          links: Seq[(Box, Box)] = Seq.empty
                        )

object BoardResponse {

  implicit object ToJSON extends Jsonable[BoardResponse] {
    override val serializers: Seq[Serializer[_]] =
      implicitly[Jsonable[Box]].serializers ++
      implicitly[Jsonable[Layout]].serializers ++
      implicitly[Jsonable[ViewTree]].serializers :+
      LinkSer

    object LinkSer extends CustomSerializer[Box Tuple2 Box](_ => ( {
      case _ => throw new NotImplementedError("Not deserializable")
    }, {
      case (box1: Box, box2: Box) => JArray(List(JString(box1.title), JString(box2.title)))
    }
    ))

  }

}