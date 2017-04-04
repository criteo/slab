package com.criteo.slab.lib

import java.io._
import java.net._

import com.criteo.slab.core._
import com.criteo.slab.utils
import com.criteo.slab.utils.{HttpClient, Jsonable}
import org.joda.time.{DateTime, Duration}
import org.json4s.DefaultFormats

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class GraphiteStore(
                     host: String,
                     port: Int,
                     webHost: String,
                     checkInterval: Duration,
                     group: Option[String] = None
                   )(implicit ec: ExecutionContext) extends ValueStore {

  import GraphiteStore._

  private val jsonFormat = DefaultFormats ++ Jsonable[GraphiteMetric].serializers

  private val DateFormat = "HH:mm_YYYYMMdd"

  private val GroupPrefix = group.map(_ + ".").getOrElse("")

  override def upload(id: String, values: Metrical.Type): Future[Unit] = {
    utils.collectTries(values.toList.map { case (name, value) =>
      send(host, port, s"$GroupPrefix$id.$name", value)
    }) match {
      case Success(_) => Future.successful()
      case Failure(e) => Future.failed(e)
    }
  }

  override def fetch(id: String, context: Context): Future[Metrical.Type] = {
    val query = HttpClient.makeQuery(Map(
      "target" -> s"$GroupPrefix$id.*",
      "from" -> s"${context.when.toString(DateFormat)}",
      "until" -> s"${context.when.plus(checkInterval).toString(DateFormat)}",
      "format" -> "json"
    ))
    val url = new URL(s"$webHost/render$query")
    HttpClient.get[String](url, Map.empty).flatMap {
      content =>
        Jsonable.parse[List[GraphiteMetric]](content, jsonFormat) match {
          case Success(metrics) =>
            val pairs = transformMetrics(s"$GroupPrefix$id", metrics)
            if (pairs.isEmpty)
              Future.failed(MissingValueException(s"cannot fetch metric for $GroupPrefix$id"))
            else
              Future.successful(pairs)
          case Failure(e) => Future.failed(e)
        }
    }
  }

  override def fetchBetween(id: String, from: DateTime, until: DateTime): Future[List[(Metrical.Type, Long)]] = {
    val query = HttpClient.makeQuery(Map(
      "target" -> s"$GroupPrefix$id.*",
      "from" -> s"${from.toString(DateFormat)}",
      "until" -> s"${until.toString(DateFormat)}",
      "format" -> "json",
      "noNullPoints" -> "true",
      "maxDataPoints" -> "100"
    ))
    val url = new URL(s"$webHost/render$query")
    HttpClient.get[String](url, Map.empty).flatMap {
      content =>
        Jsonable.parse[List[GraphiteMetric]](content, jsonFormat) match {
          case Success(metrics) =>
            val pairs = collectMetrics(s"$GroupPrefix$id", metrics)
            Future.successful(pairs)
          case Failure(e) => Future.failed(e)
        }
    }
  }

  override def fetchHistory(id: String, from: DateTime, until: DateTime): Future[Map[Long, Metrical.Type]] = {
    val query = HttpClient.makeQuery(Map(
      "target" -> s"$GroupPrefix$id.*",
      "from" -> s"${from.toString(DateFormat)}",
      "until" -> s"${until.toString(DateFormat)}",
      "format" -> "json",
      "noNullPoints" -> "true"
    ))
    val url = new URL(s"$webHost/render$query")
    HttpClient.get[String](url, Map.empty) flatMap { content =>
      Jsonable.parse[List[GraphiteMetric]](content, jsonFormat) map { metrics =>
        groupMetrics(s"$GroupPrefix$id", metrics)
      } match {
        case Success(metrics) => Future.successful(metrics)
        case Failure(e) => Future.failed(e)
      }
    }
  }
}

case class MissingValueException(message: String) extends Exception(message)

object GraphiteStore {
  def send(host: String, port: Int, target: String, value: Double): Try[Unit] = {
    Try {
      val socket = new Socket(InetAddress.getByName(host), port)
      val ps = new PrintStream(socket.getOutputStream)
      ps.println(s"$target $value ${DateTime.now.getMillis / 1000}")
      ps.flush
      socket.close
    }
  }

  // collect non empty metrics with timestamp
  def collectMetrics(prefix: String, metrics: List[GraphiteMetric]): List[(Metrical.Type, Long)] = {
    metrics
      .map(metric => metric.datapoints.map(metric.target.stripPrefix(s"${prefix}.") -> _))
      .transpose
      .map { xs =>
        val metrics = xs.collect { case (name, DataPoint(Some(value), _)) =>
          name -> value
        }.toMap
        (metrics, xs.head._2.timestamp * 1000) // Graphite uses timestamps in seconds
      }
      .filter(_._1.nonEmpty)
  }

  // take first defined DataPoint of each metric
  def transformMetrics(prefix: String, metrics: List[GraphiteMetric]): Metrical.Type = {
    val pairs = metrics
      .map { metric =>
        metric.datapoints
          .find(_.value.isDefined)
          .map(dp => (metric.target.stripPrefix(s"${prefix}."), dp.value.get))
      }
      .flatten
      .toMap
    // returns metrics when all available or nothing
    if (pairs.size != metrics.size)
      Map.empty
    else
      pairs
  }

  // group metrics by timestamp
  def groupMetrics(prefix: String, metrics: List[GraphiteMetric]): Map[Long, Metrical.Type] = {
    metrics
      .view
      .flatMap { metric =>
        val name = metric.target.stripPrefix(s"${prefix}.")
        metric
          .datapoints
          .view
          .filter(_.value.isDefined)
          .map { dp =>
            (name, dp.value.get, dp.timestamp * 1000)
          }
          .force
      }
      .groupBy(_._3)
      .mapValues(_.map { case (name, value, _) => (name, value) }.toMap)
  }
}
