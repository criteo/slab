package com.criteo.slab.utils

import java.net.{URL, URLEncoder}
import java.time.Instant

import lol.http._
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

object HttpClient {
  val log = LoggerFactory.getLogger(this.getClass)

  def get[A: ContentDecoder](url: URL, headers: Map[HttpString, HttpString] = Map.empty)(implicit ec: ExecutionContext) = {
    val defaultHeaders = Map(
      HttpString("Host") -> HttpString(url.getHost)
    )
    val path = url.getPath + Option(url.getQuery).map("?" + _).getOrElse("")
    val port = if (url.getPort > 0) url.getPort else url.getDefaultPort
    val request = Get(path).addHeaders(defaultHeaders ++ headers)
    log.info(s"Requesting $url")
    val start = Instant.now
    Client(url.getHost, port, url.getProtocol) runAndStop { client =>
      client.run(request) { res =>
        log.info(s"Response from $url, status: ${res.status}, ${Instant.now.toEpochMilli - start.toEpochMilli}ms")
        if (res.status < 400)
          res.readAs[A]
        else
          Future.failed(FailedRequestException(res))
      } recoverWith {
        case e =>
          log.error(s"Request $url failed: ${e.getMessage}")
          Future.failed(e)
      }
    }
  }

  def makeQuery(queries: Map[String, String]): String =
    "?" + queries.map { case (key, value) => encodeURI(key) + "=" + encodeURI(value) }.mkString("&")

  private def encodeURI(in: String) = URLEncoder.encode(in, "UTF-8").replace("+", "%20")
}

case class FailedRequestException(response: Response) extends Exception
