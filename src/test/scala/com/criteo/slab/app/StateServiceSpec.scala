package com.criteo.slab.app

import java.time.Instant

import com.criteo.slab.core.{BoardView, Status}
import org.scalatest.{FlatSpec, Matchers}

class StateServiceSpec extends FlatSpec with Matchers {
  "getStatsByDay" should "aggregate stats by day" in {
    val res = StateService.getStatsByDay(
      Map(
        0L -> BoardView("board0", Status.Warning, "", Seq.empty),
        100L -> BoardView("board1", Status.Success, "", Seq.empty),
        86400001L -> BoardView("board2", Status.Error, "", Seq.empty)
      )
    )
    res shouldEqual Map(
      0L -> Stats(
        1,
        1,
        0,
        2
      ),
      86400000L -> Stats(
        0,
        0,
        1,
        1
      )
    )
  }
}
