package com.criteo.slab.utils

import org.scalatest.{FlatSpec, Matchers}

import scala.util.{Failure, Success}

class packageSpec extends FlatSpec with Matchers {

  "collectTries" should "collect all successful values" in {
    collectTries(
      List(Success(1), Success(2))
    ) shouldEqual Success(List(1, 2))
  }
  "collectTries" should "stops at first failure" in {
    val e = new Exception("e")
    collectTries(
      List(Success(1), Failure(e))
    ) shouldEqual Failure(e)
  }
}
