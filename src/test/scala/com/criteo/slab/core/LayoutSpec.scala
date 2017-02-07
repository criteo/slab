package com.criteo.slab.core

import org.scalatest.{FlatSpec, Matchers}
import com.criteo.slab.utils.Jsonable

class LayoutSpec extends FlatSpec with Matchers {

  import Jsonable._

  "Layout" should "be serializable to JSON" in {
    val layout = Layout(List(
      Column(50, List(Row("A", 25, List(Box("box1", Seq.empty, vs => vs.head)))))
    ))
    layout.toJSON shouldEqual """{"columns":[{"percentage":50,"rows":[{"title":"A","percentage":25,"boxes":["box1"]}]}]}"""
  }
}
