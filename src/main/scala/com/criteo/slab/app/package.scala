package com.criteo.slab

import com.criteo.slab.core.{Layout, ViewTree}
import com.criteo.slab.utils.Jsonable
import com.criteo.slab.utils.Jsonable._

import scala.util.{Failure, Try}
package object app {

  case class BoardResponse(
                            view: ViewTree,
                            layout: Layout
                          )

  object BoardResponse {
    implicit def boardResponseJSON = new Jsonable[BoardResponse] {
      override def parse(in: String): Try[BoardResponse] = Failure(new NotImplementedError("deserialization not implemented"))

      override def serialize(in: BoardResponse): String =
        s"""{"view":${in.view.toJSON},"layout":${in.layout.toJSON}}"""
    }
  }

}
