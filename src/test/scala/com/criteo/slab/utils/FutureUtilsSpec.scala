package com.criteo.slab.utils

import com.criteo.slab.helper.FutureTests
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future

class FutureUtilsSpec extends FlatSpec with Matchers with FutureTests {
  "collect" should "put all results in a list if all futures are resolved" in {
    whenReady(
      FutureUtils.join(1 to 10 map (Future.successful(_)))
    ) { res =>
      res shouldEqual (1 to 10).toList
    }
  }

  "collect" should "return the first exception if there is any" in {
    whenReady(
      FutureUtils.join(List(
        Future.successful(1),
        Future.failed(new Exception("exp")),
        Future.successful(2)
      )).failed
    ) { res =>
      res shouldBe an[Exception]
    }
  }
}
