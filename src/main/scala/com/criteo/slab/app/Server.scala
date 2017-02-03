package com.criteo.slab.app

import com.criteo.slab.core.{Board, Layout, ValueStore}
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.{Controller, HttpServer}
import com.twitter.finatra.http.filters.CommonFilters
import com.twitter.finatra.http.routing.HttpRouter
import com.criteo.slab.utils.Jsonable._

trait Server extends HttpServer {
  val layout: Layout
  val board: Board
  implicit val valueStore: ValueStore

  override def configureHttp(router: HttpRouter) = {
    router
      .filter[CommonFilters]
      .add(new Controller {
        get("/api/board/now") { _: Request =>
          board
            .apply(None)
            .map(v => response.ok.json(v.toJSON))
        }

        get("/:*") { request: Request =>
          response.ok.fileOrIndex(
            request.params("*"),
            "index.html"
          )
        }
      })
  }
}
