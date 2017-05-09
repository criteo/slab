package com.criteo.slab.utils

import java.net.{URL, URLEncoder}
import java.time.Instant

import lol.http._
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

/** Http utilities
  *
  */
object HttpUtils {
  private val logger = LoggerFactory.getLogger(this.getClass)

  /** Send a GET request
    *
    * @param url The URL
    * @param headers The request headers
    * @param ec The [[ExecutionContext]]
    * @tparam A The result type which should provide a content decoder implementation
    * @return The result wrapped in [[Future]]
    */
  def get[A: ContentDecoder](url: URL, headers: Map[HttpString, HttpString] = Map.empty)(implicit ec: ExecutionContext): Future[A] = {
    val defaultHeaders = Map(
      HttpString("Host") -> HttpString(url.getHost)
    )
    val path = url.getPath + Option(url.getQuery).map("?" + _).getOrElse("")
    val port = if (url.getPort > 0) url.getPort else url.getDefaultPort
    val request = Get(path).addHeaders(defaultHeaders ++ headers)
    val completeURL = s"${url.getProtocol}://${url.getHost}:${port}$path"
    logger.info(s"Requesting $completeURL")
    val start = Instant.now
    Client(url.getHost, port, url.getProtocol) runAndStop { client =>
      client.run(request) { res =>
        logger.info(s"Response from $completeURL, status: ${res.status}, ${Instant.now.toEpochMilli - start.toEpochMilli}ms")
        if (res.status < 400)
          res.readAs[A]
        else
          Future.failed(FailedRequestException(res))
      } recoverWith {
        case e =>
          logger.error(s"Request $completeURL failed: ${e.getMessage}")
          Future.failed(e)
      }
    }
  }

  /** Make a HTTP Get client of which the connection is kept open
    *
    * Do not use this until lolhttp client leak issue is resolved
    * @param url The URL
    * @param ec The [[ExecutionContext]]
    * @return The client with which a GET request can be sent
    */
  def makeGet(url: URL)(implicit ec: ExecutionContext): SafeHTTPGet = {
    val port = if (url.getPort > 0) url.getPort else url.getDefaultPort
    val client = Client(url.getHost, port, url.getProtocol, maxWaiters = 1024)
    SafeHTTPGet(client, Map(
      HttpString("Host") -> HttpString(s"${url.getHost}:$port")
    ))
  }

  /** Make a query string
    *
    * @param queries The queries in key-value paris
    * @return The query string beginning with "?"
    */
  def makeQuery(queries: Map[String, String]): String =
    "?" + queries.map { case (key, value) => encodeURI(key) + "=" + encodeURI(value) }.mkString("&")

  private def encodeURI(in: String) = URLEncoder.encode(in, "UTF-8").replace("+", "%20")

  case class FailedRequestException(response: Response) extends Exception

  case class SafeHTTPGet(client: Client, defaultHeaders: Map[HttpString, HttpString])(implicit ec: ExecutionContext) {
    def apply[A: ContentDecoder](path: String, headers: Map[HttpString, HttpString] = Map.empty): Future[A] = {
      val url = s"${client.scheme}://${client.host}:${client.port}$path"
      val request = Get(path).addHeaders(defaultHeaders ++ headers)
      logger.info(s"Requesting $url")
      val start = Instant.now
      client.run(request) { res =>
        logger.info(s"Response from $url, status: ${res.status}, ${Instant.now.toEpochMilli - start.toEpochMilli}ms")
        if (res.status < 400)
          res.readAs[A]
        else
          Future.failed(FailedRequestException(res))
      } recoverWith {
        case e =>
          logger.error(s"Request $url failed: ${e.getMessage}")
          Future.failed(e)
      }
    }
  }

}

