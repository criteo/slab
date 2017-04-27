package com.criteo.slab.example

import java.time.Duration

import com.criteo.slab.app.WebServer
import com.criteo.slab.core.{NoopValueStore, ValueStore}
import com.criteo.slab.lib.GraphiteStore
import lol.http._
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Launcher {
  val logger = LoggerFactory.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {
    implicit val store: ValueStore = sys.env.get("USE_GRAPHITE").flatMap { useGraphite =>
      if (useGraphite == "true")
        for {
          host <- sys.env.get("GRAPHITE_HOST")
          port <- sys.env.get("GRAPHITE_PORT").map(_.toInt)
          webHost <- sys.env.get("GRAPHITE_WEB_HOST")
        } yield {
          logger.info("[Slab Example] using Graphite store")
          new GraphiteStore(host, port, webHost, Duration.ofSeconds(60), Some("slab.example"))
        }
      else None
    }.getOrElse(NoopValueStore)
    new WebServer(List(SimpleBoard()), statsDays = 14).apply(8082, {
      case GET at "/api/heartbeat" => Future.successful(Response(200))
    })
  }
}
