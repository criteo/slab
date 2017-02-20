package com.criteo.slab.app

import com.criteo.slab.core.{Box, Layout, Status, View, ViewNode}
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.mock.MockitoSugar
import com.criteo.slab.utils.Jsonable._
import org.mockito.Mockito._

class BoardResponseSpec extends FlatSpec with MockitoSugar with Matchers {
  val box1 = mock[Box]
  val box2 = mock[Box]
  val response = BoardResponse(
    ViewNode(
      "root",
      View(Status.Success, "message"),
      Seq.empty,
      Some("desc")
    ),
    Layout(Seq.empty),
    Seq(box1 -> box2)
  )
  it should "serialize correctly" in {
    when(box1.title) thenReturn "box1"
    when(box2.title) thenReturn "box2"
    response.toJSON shouldEqual """{"view":{"title":"root","status":"SUCCESS","message":"message","children":[],"description":"desc"},"layout":{"columns":[]},"links":[["box1","box2"]]}"""
  }

}
