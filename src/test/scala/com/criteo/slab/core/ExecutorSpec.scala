package com.criteo.slab.core

import java.time.Instant

import com.criteo.slab.helper.FutureTests
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}

class ExecutorSpec extends FlatSpec with Matchers with FutureTests with BeforeAndAfterEach {
  override def beforeEach = {
    reset(spiedCheck1)
    reset(spiedCheck2)
  }
  val executor = Executor(board)
  "apply" should "fetch current check values if no context is given" in {
    val f = executor.apply(None)
    whenReady(f) { res =>
      verify(spiedCheck1, times(1)).apply
      verify(spiedCheck2, times(1)).apply
      res shouldEqual BoardView(
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
                "check 1 message"
              ),
              CheckView(
                "check 2",
                Status.Success,
                "check 2 message: new value"
              )
            )
          )
        )
      )
    }
  }

  "apply" should "fetch past check values if a context is given" in {
    val f = executor.apply(Some(Context(Instant.now)))
    reset(spiedCheck1)
    reset(spiedCheck2)
    whenReady(f) { res =>
      verify(spiedCheck1, never).apply
      verify(spiedCheck2, never).apply
      res shouldEqual BoardView(
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
                "check 1 message"
              ),
              CheckView(
                "check 2",
                Status.Success,
                "check 2 message: 100"
              )
            )
          )
        )
      )
    }
  }
}
