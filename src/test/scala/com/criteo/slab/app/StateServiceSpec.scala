package com.criteo.slab.app

import com.criteo.slab.core.{BoardView, Status}
import org.scalatest.{FlatSpec, Matchers}

class StateServiceSpec extends FlatSpec with Matchers {
  "getStatsByHour" should "aggregate stats by hour" in {
    val res = StateService.getStatsByHour(
      Seq(
        0L -> BoardView("board0", Status.Warning, "", Seq.empty),
        100L -> BoardView("board1", Status.Success, "", Seq.empty),
        60 * 60 * 1000L -> BoardView("board2", Status.Error, "", Seq.empty),
        200L -> BoardView("board3", Status.Unknown, "", Seq.empty)
      )
    )
    res should contain theSameElementsAs Seq(
      0 -> Stats(
        1,
        1,
        0,
        1,
        3
      ),
      3600000L -> Stats(
        0,
        0,
        1,
        0,
        1
      )
    )
  }
}
