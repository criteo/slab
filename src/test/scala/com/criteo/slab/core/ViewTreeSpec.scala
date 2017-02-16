package com.criteo.slab.core

import com.criteo.slab.utils.Jsonable._
import org.scalatest.{FlatSpec, Matchers}

class ViewTreeSpec extends FlatSpec with Matchers {
  val tree: ViewTree = ViewNode(
    "A",
    View(Status.Success, "up"),
    List(ViewLeaf(
      "B",
      View(Status.Unknown, "unknown")
    )),
    Some("desc")
  )
  val json = """{"title":"A","status":"SUCCESS","message":"up","children":[{"title":"B","status":"UNKNOWN","message":"unknown"}],"description":"desc"}"""
  "serialize to json" should "generate a json string" in {
    tree.toJSON shouldEqual json
  }
}
