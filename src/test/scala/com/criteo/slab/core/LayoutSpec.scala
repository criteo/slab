package com.criteo.slab.core

import org.scalatest.{FlatSpec, Matchers}
import com.criteo.slab.utils.Jsonable

class LayoutSpec extends FlatSpec with Matchers {

  import Jsonable._

  "Grid" should "serialize" in {
    val grid = Layout(List(
      Column("A", 50, List(Row(25, List(Box("box1", Seq.empty, vs => vs.head)))))
    ))
    grid.toJSON shouldEqual """{"columns":[{"title":"A","percentage":50,"rows":[{"percentage":25,"boxes":["box1"]}]}]}"""
  }
}
