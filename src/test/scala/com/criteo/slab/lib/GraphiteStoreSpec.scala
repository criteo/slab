package com.criteo.slab.lib

import java.net._
import java.util.concurrent._

import com.criteo.slab.helper.FutureTests
import org.joda.time.Duration
import org.scalatest.{FlatSpec, Matchers}

import scala.io._

class GraphiteStoreSpec extends FlatSpec with Matchers with FutureTests {
  val port = 5000
  val server = new ServerSocket(port)
  val store = new GraphiteStore("localhost", port, "http://localhost", new Duration(60 * 1000))

  val pool = Executors.newFixedThreadPool(1)

  "value store" should "be able to send metrics to Graphite server" in {
    val f = pool.submit(new Echo(server))
    store.upload("metrics", Map(
      "latency" -> 200
    ))
    f.get should startWith("metrics.latency 200")
  }

  "upload" should "returns the exception when failed" in {
    whenReady(
      store.upload("metrics", Map(
        "latency" -> 200
      )).failed
    ) { res =>
      res shouldBe a[java.net.ConnectException]
    }
  }

  "transform metrics" should "turn Graphite metrics into pairs" in {
    val metrics = List(
      GraphiteMetric(
        "metric.one",
        List(
          DataPoint(None, 2000),
          DataPoint(Some(1), 2060)
        )
      ),
      GraphiteMetric(
        "metric.two",
        List(
          DataPoint(None, 2000),
          DataPoint(Some(2), 2060)
        )
      )
    )
    GraphiteStore.transformMetrics("metric", metrics) shouldEqual Map("one" -> 1.0, "two" -> 2.0)
  }

  "transform metrics" should "return empty if some metrics are missing" in {
    val metrics = List(
      GraphiteMetric(
        "metric.one",
        List(DataPoint(Some(1), 2000))
      ),
      GraphiteMetric(
        "metric.two",
        List(DataPoint(None, 2000))
      )
    )
    GraphiteStore.transformMetrics("metric", metrics) shouldEqual Map.empty
  }

  "collect metrics" should "return non empty metrics with timestamp" in {
    val metrics = List(
      GraphiteMetric(
        "metric.one",
        List(DataPoint(Some(1), 1000), DataPoint(None, 2000))
      ),
      GraphiteMetric(
        "metric.two",
        List(DataPoint(Some(2), 1000), DataPoint(None, 2000))
      )
    )
    GraphiteStore.collectMetrics("metric", metrics) shouldEqual List(
      (Map(
        "one" -> 1.0,
        "two" -> 2.0
      ), 1000000)
    )
  }

  "group metrics" should "group metrics" in {
    val metrics = List(
      GraphiteMetric(
        "metric.one",
        List(DataPoint(Some(1), 1000), DataPoint(Some(2), 2000))
      ),
      GraphiteMetric(
        "metric.two",
        List(DataPoint(Some(3), 1000), DataPoint(Some(4), 2000))
      )
    )
    GraphiteStore.groupMetrics("metric", metrics) shouldEqual Map(
      1000000 -> Map("one" -> 1, "two" -> 3),
      2000000 -> Map("one" -> 2, "two" -> 4)
    )
  }

  class Echo(server: ServerSocket) extends Callable[String] {
    def call() = {
      val s = server.accept
      val lines = new BufferedSource(s.getInputStream).getLines
      val result = lines.mkString
      s.close
      server.close
      result
    }
  }

}