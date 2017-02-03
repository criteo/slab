package com.criteo.slab.core

import com.criteo.slab.helper.TwitterFutures
import com.twitter.util.Future
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}
import org.mockito.Mockito._

class BoardSpec extends FlatSpec with Matchers with MockitoSugar with TwitterFutures {
  implicit val store = TestStore

  val box1 = mock[Box]
  val box2 = mock[Box]
  val board = Board(
    "a test board",
    box1 :: box2 :: Nil,
    views => views.sorted.reverse.head
  )

  "apply()" should "return" in {
    val box1Node = ViewNode(
      "box1",
      View(Status.Error, "box1 down"),
      List.empty
    )
    when(box1.apply(None)) thenReturn Future.value(box1Node)
    val box2Node = ViewNode(
      "box2",
      View(Status.Success, "box1 up"),
      List.empty
    )
    when(box2.apply(None)) thenReturn Future.value(box2Node)
    whenReady(board.apply(None).toFutureConcept) { r =>
      r shouldEqual ViewNode(
        "a test board",
        View(Status.Error, "box1 down"),
        List(
          box1Node,
          box2Node
        )
      )
    }
  }
}
