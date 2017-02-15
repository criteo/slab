package com.criteo.slab

import com.criteo.slab.core.{Box, Layout, ViewTree}
import com.criteo.slab.utils.Jsonable
import com.criteo.slab.utils.Jsonable._
import org.json4s.{DefaultFormats, Extraction}
import org.json4s.native.Serialization

import scala.util.{Failure, Try}

package object app {

  case class BoardResponse(
                            view: ViewTree,
                            layout: Layout,
                            links: Seq[(Box, Box)] = Seq.empty
                          )

  object BoardResponse {
    implicit def boardResponseJSON = new Jsonable[BoardResponse] {
      implicit val formats = DefaultFormats
      override def parse(in: String): Try[BoardResponse] = Failure(new NotImplementedError("deserialization not implemented"))

      override def serialize(in: BoardResponse): String = {
        val links = Serialization.write(in.links.map{ case (from, to) =>
          Seq(from.title, to.title)
        })
        s"""{"view":${in.view.toJSON},"layout":${in.layout.toJSON},"links":$links}"""
      }
    }
  }

}
