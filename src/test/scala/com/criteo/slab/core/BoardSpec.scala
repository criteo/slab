package com.criteo.slab.core

import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}
import org.mockito.Mockito._
import shapeless.HNil

class BoardSpec extends FlatSpec with Matchers with MockitoSugar {
  val box1 = mock[Box[_]]
  when(box1.title) thenReturn "box 1"
  val box2 = mock[Box[_]]
  when(box2.title) thenReturn "box 2"

  "constructor" should "require that boxes and layout are correctly defined" in {
    intercept[IllegalArgumentException] {
      Board(
        "a broken board",
        box1 :: HNil,
        (views, _) => views.values.head,
        Layout(
          Seq(Column(
            100,
            Seq(Row("row", 10, box2 :: Nil))
          ))
        )
      )
    }
    intercept[IllegalArgumentException] {
      Board(
        "a broken board",
        box1 :: box2 :: HNil,
        (views, _) => views.values.head,
        Layout(
          Seq(Column(
            100,
            Seq(Row("row", 10, List.empty))
          ))
        )
      )
    }
  }
}
