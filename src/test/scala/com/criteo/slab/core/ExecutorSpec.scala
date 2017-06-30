package com.criteo.slab.core

import java.time.Instant

import com.criteo.slab.helper.FutureTests
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}

class ExecutorSpec extends FlatSpec with Matchers with FutureTests with BeforeAndAfterEach {
  override def beforeEach = {
    reset(spiedCheck1)
    reset(spiedCheck2)
    reset(spiedCheck3)
  }

  val executor = Executor(board)
  "apply" should "fetch current check values if no context is given" in {
    val boardView = BoardView(
      "board",
      Status.Unknown,
      "board message",
      List(
        BoxView(
          "box 1",
          Status.Success,
          "box 1 message",
          List(
            CheckView(
              "check 1",
              Status.Success,
              "check 1 message: new value"
            ),
            CheckView(
              "check 2",
              Status.Success,
              "check 2 message: new value"
            )
          )
        ),
        BoxView(
          "box 2",
          Status.Success,
          "box 2 message",
          List(
            CheckView(
              "check 3",
              Status.Success,
              "check 3 message: 3"
            )
          )
        )
      )
    )
    val f = executor.apply(None)
    whenReady(f) { res =>
      verify(spiedCheck1, times(1)).apply
      verify(spiedCheck2, times(1)).apply
      verify(spiedCheck3, times(1)).apply
      res shouldEqual boardView
    }
  }

  "apply" should "fetch past check values if a context is given" in {
    val boardView = BoardView(
      "board",
      Status.Unknown,
      "board message",
      List(
        BoxView(
          "box 1",
          Status.Success,
          "box 1 message",
          List(
            CheckView(
              "check 1",
              Status.Success,
              "check 1 message: 100"
            ),
            CheckView(
              "check 2",
              Status.Success,
              "check 2 message: 100"
            )
          )
        ),
        BoxView(
          "box 2",
          Status.Success,
          "box 2 message",
          List(
            CheckView(
              "check 3",
              Status.Success,
              "check 3 message: 100"
            )
          )
        )
      )
    )
    val f = executor.apply(Some(Context(Instant.now)))
    whenReady(f) { res =>
      verify(spiedCheck1, never).apply
      verify(spiedCheck2, never).apply
      verify(spiedCheck3, never).apply
      res shouldEqual boardView
    }
  }
}
