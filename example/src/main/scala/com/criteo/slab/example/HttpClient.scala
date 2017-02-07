package com.criteo.slab.example

import com.twitter.finagle.Http
import com.twitter.finagle.http.{RequestBuilder, Response, Status}
import com.twitter.util.Future

object HttpClient {
  def apply(uri: String): Future[Response] = {
    MockServer.httpExternalPort.fold(Future(Response(Status.BadGateway))) { port =>
      val service = Http.client.newService(s"localhost:$port")
      val request = RequestBuilder()
        .url(s"http://localhost${uri}")
        .buildGet()
      service(request)
    }
  }
}
