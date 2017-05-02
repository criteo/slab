// Example: A Slab server
//
// Guide for creating a Slab server
package com.criteo.slab.example

import java.time.Duration

import com.criteo.slab.app.WebServer
import com.criteo.slab.core.ValueStore
import com.criteo.slab.lib.{GraphiteStore, NoopValueStore}
import lol.http._
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global

object Launcher {
  val logger = LoggerFactory.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {
    // Define a value store for uploading and restoring history
    // Use NoopValueStore (which does nothing) as fallback
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
    // Define a Slab web server with the list of boards
    new WebServer(
      List(SimpleBoard()),
      // Retrieve the statistics of last 14 days after launch, which will be shown on the calendar
      statsDays = 14,
      // Define a custom route, Slab is built with [lolhttp](https://github.com/criteo/lolhttp)
      customRoutes = {
        case GET at "/api/heartbeat" => Ok("Ok")
      }
    ).apply(8082)
  }
}
