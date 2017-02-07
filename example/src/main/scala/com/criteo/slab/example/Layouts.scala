package com.criteo.slab.example

import com.criteo.slab.core.{Column, Layout, Row}

object Layouts {
  import Boxes._
  lazy val simpleBoardLayout = Layout(
    Seq(Column(
      50,
      Seq(Row("Tier 1", 100, Seq(webserver)))
    ), Column(
      50,
      Seq(
        Row("Tier 2 - 1", 50, Seq(gateway)),
        Row("Tier 2 - 2", 50, Seq(pipelineZeta, pipelineOmega))
      )
    ))
  )
}
