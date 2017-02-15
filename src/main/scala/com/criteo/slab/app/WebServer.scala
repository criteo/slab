package com.criteo.slab.app

import com.criteo.slab.core.{Board, NoopValueStore, ValueStore}
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.filters.CommonFilters
import com.twitter.finatra.http.routing.HttpRouter

class WebServer(val boards: Seq[Board])(implicit val valueStore: ValueStore = NoopValueStore) extends HttpServer {
  override def configureHttp(router: HttpRouter) = {
    router
      .filter[CommonFilters]
      .add(new SlabController(boards))
  }
}
