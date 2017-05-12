package com.criteo.slab.utils

import java.net.{URL, URLEncoder}
import java.time.Instant
import java.util.concurrent.TimeUnit.SECONDS

import lol.http._
import org.slf4j.LoggerFactory

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

/** Http utilities
  *
  */
object HttpUtils {
  private val logger = LoggerFactory.getLogger(this.getClass)

  /** Send a GET request
    *
    * @param url     The URL
    * @param headers The request headers
    * @param ec      The [[ExecutionContext]]
    * @tparam A The result type which should provide a content decoder implementation
    * @return The result wrapped in [[Future]]
    */
  def get[A: ContentDecoder](
                              url: URL,
                              headers: Map[HttpString, HttpString] = Map.empty,
                              timeout: FiniteDuration = Duration.create(60, SECONDS),
                              connectionTimeout: FiniteDuration = Duration.create(5, SECONDS)
                            )(implicit ec: ExecutionContext): Future[A] = {
    val defaultHeaders = Map(
      HttpString("Host") -> HttpString(url.getHost)
    )
    val path = url.getPath + Option(url.getQuery).map("?" + _).getOrElse("")
    val port = if (url.getPort > 0) url.getPort else url.getDefaultPort
    val request = Get(path).addHeaders(defaultHeaders ++ headers)
    val fullURL = s"${url.getProtocol}://${url.getHost}:${port}$path"
    logger.info(s"Requesting $fullURL")
    val start = Instant.now
    Client(url.getHost, port, url.getProtocol) runAndStop { client =>
      client.run(request) { res =>
        logger.info(s"Response from $fullURL, status: ${res.status}, ${Instant.now.toEpochMilli - start.toEpochMilli}ms")
        handleResponse(res, fullURL)
      } recoverWith handleError(fullURL)
    }
  }

  /** Make a HTTP Get client of which the connection is kept open
    *
    * Do not use this until lolhttp client leak issue is resolved
    *
    * @param url The URL
    * @param ec  The [[ExecutionContext]]
    * @return The client with which a GET request can be sent
    */
  def makeGet(url: URL, connectionTimeout: FiniteDuration = Duration.create(5, SECONDS))(implicit ec: ExecutionContext): SafeHTTPGet = {
    val port = if (url.getPort > 0) url.getPort else url.getDefaultPort
    val client = Client(url.getHost, port, url.getProtocol, maxWaiters = 1024, connectionTimeout = connectionTimeout)
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

  private def handleResponse[A: ContentDecoder](res: Response, url: String)(implicit ec: ExecutionContext): Future[A] = {
    if (res.status < 400)
      res.readAs[A]
    else
      res
        .readAs[String]
        .recoverWith { case e =>
          logger.error(e.getMessage, e)
          Future.successful("Unable to get the message")
        }
        .flatMap { message =>
          logger.info(s"Request to $url has failed, status: ${res.status}, message: $message")
          Future.failed(FailedRequestException(res))
        }
  }

  private def handleError[A](url: String)(implicit ec: ExecutionContext): PartialFunction[Throwable, Future[A]] = {
    case f: FailedRequestException => Future.failed(f)
    case NonFatal(e) =>
      logger.error(s"Error when requesting $url: ${e.getMessage}", e)
      Future.failed(e)
  }

  /** A response with status >= 400
    *
    * @param response The [[Response]]
    */
  case class FailedRequestException(response: Response) extends Exception

  /**
    * It be used to send GET requests using the same client
    *
    * @param client         The client
    * @param defaultHeaders Default request headers
    * @param ec             [[ExecutionContext]]
    */
  case class SafeHTTPGet(client: Client, defaultHeaders: Map[HttpString, HttpString])(implicit ec: ExecutionContext) {
    def apply[A: ContentDecoder](
                                  path: String,
                                  headers: Map[HttpString, HttpString] = Map.empty,
                                  timeout: FiniteDuration = Duration.create(60, SECONDS)
                                ): Future[A] = {
      val fullURL = s"${client.scheme}://${client.host}:${client.port}$path"
      val request = Get(path).addHeaders(defaultHeaders ++ headers)
      logger.info(s"Requesting $fullURL")
      val start = Instant.now
      client.run(request) { res: Response =>
        logger.info(s"Response from $fullURL, status: ${res.status}, ${Instant.now.toEpochMilli - start.toEpochMilli}ms")
        handleResponse(res, fullURL)
      } recoverWith handleError(fullURL)
    }
  }

}

