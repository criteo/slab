package com.criteo.slab.lib

import com.criteo.slab.utils.Jsonable
import org.json4s.DefaultFormats
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Success

class GraphiteMetricSpec extends FlatSpec with Matchers {
  "JSON serializer" should "be able to read json" in {
    val json = """[{"target":"metric.one", "datapoints":[[1.0, 2000], [null, 2060]]}]""".stripMargin.replace("\n", "")
    val formats = DefaultFormats ++ Jsonable[GraphiteMetric].serializers
    val r = Jsonable.parse[List[GraphiteMetric]](json, formats)
    r shouldEqual Success(List(GraphiteMetric("metric.one", List(
      DataPoint(Some(1.0), 2000),
      DataPoint(None, 2060)
    ))))
  }
}
