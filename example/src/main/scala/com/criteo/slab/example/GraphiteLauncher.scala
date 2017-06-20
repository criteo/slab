package com.criteo.slab.example

import java.time.Duration

import com.criteo.slab.app.WebServer
import com.criteo.slab.lib.graphite.GraphiteStore
import org.slf4j.LoggerFactory
import com.criteo.slab.lib.graphite.GraphiteCodecs

object GraphiteLauncher {
  val logger = LoggerFactory.getLogger(this.getClass)

  import scala.concurrent.ExecutionContext.Implicits.global
  import SimpleBoard._
  import GraphiteCodecs._

  def main(args: Array[String]): Unit = {
    val maybeStore = for {
      host <- sys.env.get("GRAPHITE_HOST")
      port <- sys.env.get("GRAPHITE_PORT").map(_.toInt)
      webHost <- sys.env.get("GRAPHITE_WEB_HOST")
    } yield new GraphiteStore(host, port, webHost, Duration.ofSeconds(60), Some("slab.example"))
    implicit val store = maybeStore match {
      case Some(s) =>
        logger.info("[Slab Example] using Graphite store")
        s
      case None =>
        logger.error("Graphite store is not set up")
        sys.exit(1)
    }

    WebServer(statsDays = 14)
      .attach(board)
      .apply(8080)
  }
}
