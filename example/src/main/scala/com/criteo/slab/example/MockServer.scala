package com.criteo.slab.example

import com.criteo.slab.app.SlabController
import com.criteo.slab.core.NoopValueStore
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.filters.CommonFilters
import com.twitter.finatra.http.routing.HttpRouter

// A server that mocks remote endpoints to be checked
object MockServer extends HttpServer {

  import Boards._
  import Layouts._

  implicit val valueStore = NoopValueStore

  override protected def configureHttp(router: HttpRouter): Unit = {
    router
      .filter[CommonFilters]
      .add(MockController)
      .add(new SlabController(
        List(
          simpleBoard -> simpleBoardLayout
        )
      ))
  }
}
