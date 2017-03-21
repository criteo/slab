package com.criteo.slab.core

import com.criteo.slab.utils.Jsonable._
import org.scalatest.{FlatSpec, Matchers}

class TimeSeriesSpec extends FlatSpec with Matchers {
  "TimeSeries" should "be serializable to JSON" in {
    TimeSeries(
      "t1",
      List(
        Point(1.0, 1000),
        Point(2.0, 2000)
      )
    ).toJSON shouldEqual """{"title":"t1","data":[[1.0,1000],[2.0,2000]]}"""
  }
}
