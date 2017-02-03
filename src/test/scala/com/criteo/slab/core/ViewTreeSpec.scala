package com.criteo.slab.core

import com.criteo.slab.utils.Jsonable
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Success

class ViewTreeSpec extends FlatSpec with Matchers {
  import Jsonable._
  val tree: ViewTree = ViewNode(
    "A",
    View(Status.Success, "up"),
    List(ViewLeaf(
      "B",
      View(Status.Unknown, "unknown")
    ))
  )
  val json = """{"title":"A","status":"SUCCESS","message":"up","children":[{"title":"B","status":"UNKNOWN","message":"unknown"}]}"""
  "serialize to json" should "generate a json string" in {
    tree.toJSON shouldEqual json
  }

  "deserialize" should "parse a ViewTree from a string" in {
    val json = """{"title":"A","status":"SUCCESS","message":"up","children":[{"title":"B","status":"UNKNOWN","message":"unknown"}]}"""
    json.parseTo[ViewTree] shouldEqual Success(tree)
  }
}
