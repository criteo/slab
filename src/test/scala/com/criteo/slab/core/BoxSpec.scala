package com.criteo.slab.core

import java.time.Instant

import com.criteo.slab.helper.FutureTests
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

class BoxSpec extends FlatSpec with Matchers with MockitoSugar with FutureTests {

  implicit val store = TestStore

  val aggregator = (views: Seq[View]) => {
    views.sorted.reverse.head
  }
  val spiedVersionCheck = spy(versionCheck)
  val spiedLatencyCheck = spy(latencyCheck)
  val checkGroup = Box(
    "test box",
    spiedVersionCheck :: spiedLatencyCheck :: Nil,
    aggregator
  )

  "apply() with no context" should "check current values and aggregate" in {
    whenReady(checkGroup.apply(None)) { r =>
      verify(spiedVersionCheck).now
      verify(spiedLatencyCheck).now
      r shouldEqual BoxView(
        "test box",
        Status.Warning,
        "latency 2000",
        List(
          CheckView(
            "app version",
            Status.Success,
            "version 9000"
          ),
          CheckView(
            "app latency",
            Status.Warning,
            "latency 2000"
          )
        )
      )
    }
  }

  "apply() with context" should "check values with the given context" in {
    val context = Context(Instant.ofEpochMilli(1000))
    whenReady(checkGroup.apply(Some(context))) { r =>
      verify(spiedVersionCheck).replay(context)
      verify(spiedLatencyCheck).replay(context)
      r shouldEqual BoxView(
        "test box",
        Status.Warning,
        "latency 1000",
        List(
          CheckView(
            "app version",
            Status.Success,
            "version 1000"
          ),
          CheckView(
            "app latency",
            Status.Warning,
            "latency 1000"
          )
        )
      )
    }
  }
}
