package com.criteo.slab.app

import com.criteo.slab.core.{Board, Layout, ValueStore}
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.filters.CommonFilters
import com.twitter.finatra.http.routing.HttpRouter

class WebServer(val settings: Seq[(Board, Layout)])(implicit val valueStore: ValueStore) extends HttpServer {
  override def configureHttp(router: HttpRouter) = {
    router
      .filter[CommonFilters]
      .add(new SlabController(settings))
  }
}
