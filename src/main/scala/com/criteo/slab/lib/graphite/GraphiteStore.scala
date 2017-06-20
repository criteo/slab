package com.criteo.slab.lib.graphite

import java.io._
import java.net._
import java.time._
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit.SECONDS

import com.criteo.slab.core._
import com.criteo.slab.lib.graphite.GraphiteStore.Repr
import com.criteo.slab.utils
import com.criteo.slab.utils.{HttpUtils, Jsonable}
import org.json4s.DefaultFormats
import org.slf4j.LoggerFactory

import scala.concurrent.duration.{FiniteDuration, Duration => CDuration}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
  * A value store that uses Graphite
  *
  * @param host              The host of the writing endpoint
  * @param port              The port of the writing endpoint
  * @param webHost           The URL of the Web host for reading
  * @param checkInterval     Check interval in [[java.time.Duration Duration]]
  * @param group             The group name of Graphite metrics
  * @param serverTimeZone    The timezone of the server
  * @param connectionTimeout Connection timeout
  * @param requestTimeout    Request timeout
  * @param maxConnections    Max connections of the Http client
  * @param ec                The execution context
  */
class GraphiteStore(
                     host: String,
                     port: Int,
                     webHost: String,
                     checkInterval: Duration,
                     group: Option[String] = None,
                     serverTimeZone: ZoneId = ZoneId.systemDefault(),
                     connectionTimeout: FiniteDuration = CDuration.create(30, SECONDS),
                     requestTimeout: FiniteDuration = CDuration.create(60, SECONDS),
                     maxConnections: Int = 32
                   )(implicit ec: ExecutionContext) extends Store[Repr] {

  import GraphiteStore._

  private val logger = LoggerFactory.getLogger(this.getClass)

  private val jsonFormat = DefaultFormats ++ Jsonable[GraphiteMetric].serializers

  private val DateFormatter = DateTimeFormatter.ofPattern("HH:mm_YYYYMMdd").withZone(serverTimeZone)

  private val GroupPrefix = group.map(_ + ".").getOrElse("")

  private val Get = HttpUtils.makeGet(new URL(webHost), connectionTimeout = connectionTimeout, maxConnections = maxConnections)

  // Returns a prefix of Graphite metrics in "groupId.id"
  private def getPrefix(id: String) = GroupPrefix + id

  override def upload[T](id: String, context: Context, v: T)(implicit codec: Codec[T, Repr]): Future[Unit] = {
    utils.collectTries(codec.encode(v).toList.map { case (name, value) =>
      send(host, port, s"${getPrefix(id)}.$name", value)
    }) match {
      case Success(_) =>
        logger.debug(s"succeeded in uploading $GroupPrefix$id")
        Future.successful()
      case Failure(e) =>
        logger.debug(s"failed to upload $GroupPrefix$id", e)
        Future.failed(e)
    }
  }


  override def fetch[T](id: String, context: Context)(implicit codec: Codec[T, Repr]): Future[T] = {
    val query = HttpUtils.makeQuery(Map(
      "target" -> s"${getPrefix(id)}.*",
      "from" -> s"${DateFormatter.format(context.when)}",
      "until" -> s"${DateFormatter.format(context.when.plus(checkInterval))}",
      "format" -> "json"
    ))
    Get[String](s"/render$query", Map.empty, requestTimeout) flatMap { content =>
      Jsonable.parse[List[GraphiteMetric]](content, jsonFormat) match {
        case Success(metrics) =>
          val pairs = transformMetrics(s"${getPrefix(id)}", metrics)
          if (pairs.isEmpty)
            Future.failed(MissingValueException(s"cannot fetch metric for ${getPrefix(id)}"))
          else
            codec.decode(pairs) match {
              case Success(v) => Future.successful(v)
              case Failure(e) => Future.failed(e)
            }
        case Failure(e) => Future.failed(e)
      }
    }
  }


  override def fetchHistory[T](id: String, from: Instant, until: Instant)(implicit codec: Codec[T, Repr]): Future[Seq[(Long, T)]] = {
    val query = HttpUtils.makeQuery(Map(
      "target" -> s"${getPrefix(id)}.*",
      "from" -> s"${DateFormatter.format(from)}",
      "until" -> s"${DateFormatter.format(until)}",
      "format" -> "json",
      "noNullPoints" -> "true"
    ))
    Get[String](s"/render$query", Map.empty, requestTimeout) flatMap { content =>
      Jsonable.parse[List[GraphiteMetric]](content, jsonFormat) map { metrics =>
        groupMetrics(s"${getPrefix(id)}", metrics)
      } match {
        case Success(metrics) =>
          logger.debug(s"${getPrefix(id)}: fetched ${metrics.size} values")
          Future.successful {
            metrics.mapValues(codec.decode(_)).collect {
              case (ts, Success(v)) => (ts, v)
            }.toList
          }
        case Failure(e) =>
          logger.debug(s"${getPrefix(id)}: Invalid graphite metric, got $content")
          Future.failed(e)
      }
    }
  }
}


object GraphiteStore {
  type Repr = Map[String, Double]
  def send(host: String, port: Int, target: String, value: Double): Try[Unit] = {
    Try {
      val socket = new Socket(InetAddress.getByName(host), port)
      val ps = new PrintStream(socket.getOutputStream)
      ps.println(s"$target $value ${Instant.now.toEpochMilli / 1000}")
      ps.flush
      socket.close
    }
  }

  // take first defined DataPoint of each metric
  def transformMetrics(prefix: String, metrics: List[GraphiteMetric]): Repr = {
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
  def groupMetrics(prefix: String, metrics: List[GraphiteMetric]): Map[Long, Repr] = {
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

  case class MissingValueException(message: String) extends Exception(message)

}
