package com.criteo.slab.core

import com.criteo.slab.helper.FutureTests
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}
import org.mockito.Mockito._

import scala.concurrent.Future

class BoardSpec extends FlatSpec with Matchers with MockitoSugar with FutureTests {
  implicit val store = TestStore

  val box1 = mock[Box]
  val box2 = mock[Box]
  val board = Board(
    "a test board",
    box1 :: box2 :: Nil,
    views => views.sorted.reverse.head,
    Layout(
      Seq(
        Column(100, Seq(
          Row("col-1-row-1", 100, Seq(box1, box2))
        ))
      )
    )
  )

  "constructor" should "require that boxes and layout are correctly defined" in {
    val exception = intercept[IllegalArgumentException] {
      Board(
        "a broken board",
        box1 :: Nil,
        views => views.head,
        Layout(
          Seq(Column(
            100,
            Seq(Row("row", 10, box2 :: Nil))
          )
        ))
      )
    }
    exception.getMessage shouldEqual "requirement failed: Board definition error, please make sure all boxes are present both in board and layout"
  }

  "apply()" should "return a ViewTree" in {
    val box1Node = ViewNode(
      "box1",
      View(Status.Error, "box1 down"),
      List.empty
    )
    when(box1.apply(None)) thenReturn Future(box1Node)
    val box2Node = ViewNode(
      "box2",
      View(Status.Success, "box1 up"),
      List.empty
    )
    when(box2.apply(None)) thenReturn Future(box2Node)
    whenReady(board.apply(None)) { r =>
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
