package com.criteo.slab.example

import com.criteo.slab.core.Board

object Boards {

  import Boxes._
  import Layouts._
  val simpleBoard = Board(
    "Example board",
    Seq(webserver, gateway, pipelineZeta, pipelineOmega),
    takeHighestLevel,
    simpleBoardLayout
  )
}
