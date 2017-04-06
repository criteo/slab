package com.criteo.slab.core

import com.criteo.slab.utils.Jsonable._
import org.scalatest.{FlatSpec, Matchers}

class ViewTreeSpec extends FlatSpec with Matchers {
  "serialize to json" should "generate a json string" in {
    val tree: ViewTree = ViewNode(
      "A",
      View(Status.Success, "up"),
      List(ViewLeaf(
        "B",
        View(Status.Unknown, "unknown", Some("lbl"))
      )),
      Some("desc"),
      Some(1)
    )
    val json =
      """
        |{
        |"title":"A",
        |"status":"SUCCESS",
        |"message":"up",
        |"children":[{"title":"B","status":"UNKNOWN","message":"unknown","label":"lbl"}],
        |"description":"desc",
        |"labelLimit":1
        |}""".stripMargin.replace("\n", "")
    tree.toJSON shouldEqual json
  }
  "serialize to json" should "set labelLimit to children's size if not specified" in {
    val tree: ViewTree = ViewNode(
      "A",
      View(Status.Success, "up"),
      List(
        ViewLeaf(
          "B",
          View(Status.Unknown, "unknown", Some("lbl"))
        ),
        ViewLeaf(
          "C",
          View(Status.Unknown, "unknown", Some("lbl"))
        )
      ),
      Some("desc"),
      None
    )
    val json =
      """
        |{
        |"title":"A",
        |"status":"SUCCESS",
        |"message":"up",
        |"children":[
        |{"title":"B","status":"UNKNOWN","message":"unknown","label":"lbl"},
        |{"title":"C","status":"UNKNOWN","message":"unknown","label":"lbl"}
        |],
        |"description":"desc",
        |"labelLimit":2
        |}""".stripMargin.replace("\n", "")
    tree.toJSON shouldEqual json
  }
}
