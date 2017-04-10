package com.criteo.slab.app

import com.criteo.slab.core.{BoardView, Status}
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.{FlatSpec, Matchers}

class StateServiceSpec extends FlatSpec with Matchers {
  "getStatsByDay" should "work" in {
    val res = StateService.getStatsByDay(
      Map(
        new DateTime(3000, 1, 1, 12, 30, DateTimeZone.UTC).getMillis -> BoardView("board1", Status.Success, "", Seq.empty),
        new DateTime(3000, 1, 2, 0, 30, DateTimeZone.UTC).getMillis -> BoardView("board2", Status.Error, "", Seq.empty)
      )
    )
    res shouldEqual Map(
      new DateTime(3000, 1, 1, 0, 0, DateTimeZone.UTC).getMillis -> Stats(
        1,
        0,
        0,
        1
      ),
      new DateTime(3000, 1, 2, 0, 0, DateTimeZone.UTC).getMillis -> Stats(
        0,
        0,
        1,
        1
      )
    )
  }
}
