package com.criteo.slab.example

import com.criteo.slab.app.WebServer
import com.criteo.slab.core.{NoopValueStore, ValueStore}
import com.criteo.slab.lib.GraphiteStore
import org.joda.time.Duration

import scala.concurrent.ExecutionContext.Implicits.global

object Launcher {
  def main(args: Array[String]): Unit = {
    implicit val store: ValueStore = sys.env.get("USE_GRAPHITE").flatMap { useGraphite =>
      if (useGraphite == "true")
        for {
          host <- sys.env.get("GRAPHITE_HOST")
          port <- sys.env.get("GRAPHITE_PORT").flatMap(p => Some(p.toInt))
          webHost <- sys.env.get("GRAPHITE_WEB_HOST")
        } yield {
          new GraphiteStore(host, port, webHost, new Duration(60 * 1000), Some("slab"))
        }
      else None
    }.getOrElse(NoopValueStore)
    new WebServer(List(SimpleBoard())).apply(8082)
  }
}
