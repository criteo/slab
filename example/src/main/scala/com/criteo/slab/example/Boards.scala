package com.criteo.slab.example

import com.criteo.slab.core.Board

object Boards {

  import Boxes._
  val simpleBoard = Board(
    "Example board",
    Seq(webserver, gateway, pipelineZeta, pipelineOmega),
    takeHighestLevel
  )
}
