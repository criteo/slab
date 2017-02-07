package com.criteo.slab.example

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller

object MockController extends Controller {
  get("/echo/:value") { req: Request =>
    val value = req.params.getOrElse("value", "")
    response.ok(value).toFuture
  }
}
