package com.criteo.slab.core

import com.criteo.slab.helper.TwitterFutures
import org.joda.time.DateTime
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}
import org.mockito.Mockito._

class BoxSpec extends FlatSpec with Matchers with MockitoSugar with TwitterFutures {

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
    whenReady(checkGroup.apply(None).toFutureConcept) { r =>
      verify(spiedVersionCheck).now
      verify(spiedLatencyCheck).now
      r shouldEqual ViewNode(
        "test box",
        View(Status.Warning, "latency 2000"),
        List(
          ViewLeaf(
            "app version",
            View(Status.Success, "version 9000")
          ),
          ViewLeaf(
            "app latency",
            View(Status.Warning, "latency 2000")
          )
        )
      )
    }
  }

  "apply() with context" should "check values with the given context" in {
    val context = Context(new DateTime(1000))
    whenReady(checkGroup.apply(Some(context)).toFutureConcept) { r =>
      verify(spiedVersionCheck).replay(context)
      verify(spiedLatencyCheck).replay(context)
      r shouldEqual ViewNode(
        "test box",
        View(Status.Warning, "latency 1000"),
        List(
          ViewLeaf(
            "app version",
            View(Status.Success, "version 1000")
          ),
          ViewLeaf(
            "app latency",
            View(Status.Warning, "latency 1000")
          )
        )
      )
    }
  }
}
