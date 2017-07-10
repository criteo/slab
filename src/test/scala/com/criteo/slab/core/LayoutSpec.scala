package com.criteo.slab.core

import com.criteo.slab.utils.Jsonable._
import org.scalatest.{FlatSpec, Matchers}

class LayoutSpec extends FlatSpec with Matchers {

  "Layout" should "be serializable to JSON" in {
    val layout = Layout(
      Column(50, Row("A", 25, List(
        Box[String]("box1", check1 :: Nil, (vs, _) => vs.head._2.view)
      )))
    )
    layout.toJSON shouldEqual """{"columns":[{"percentage":50.0,"rows":[{"title":"A","percentage":25.0,"boxes":[{"title":"box1","labelLimit":64}]}]}]}"""
  }
}
