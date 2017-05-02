package com.criteo.slab.utils

import java.net.{URL, URLEncoder}
import java.time.Instant

import lol.http._
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

/** A Http client
  *
  */
object HttpUtils {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def makeGet(url: URL)(implicit ec: ExecutionContext) = {
    val port = if (url.getPort > 0) url.getPort else url.getDefaultPort
    val client = Client(url.getHost, port, url.getProtocol)
    SafeHTTPGet(client, Map(
      HttpString("Host") -> HttpString(s"${url.getHost}:$port")
    ))
  }

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
      client(request) flatMap { res =>
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

