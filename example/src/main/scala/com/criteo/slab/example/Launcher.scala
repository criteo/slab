// Example: A Slab server
//
// Guide for creating a Slab server
package com.criteo.slab.example

import com.criteo.slab.app.WebServer
import com.criteo.slab.lib.InMemoryStore
import lol.http._

object Launcher {

  import SimpleBoard._

  import scala.concurrent.ExecutionContext.Implicits.global

  // You should provide codec for checked value types for values to be persistent in a store
  import InMemoryStore._

  def main(args: Array[String]): Unit = {
    // Define a value store for uploading and restoring history
    implicit val store = new InMemoryStore

    // Create a webserver
    WebServer(
      // You can define a custom route, Slab web server is built with [lolhttp](https://github.com/criteo/lolhttp)
      customRoutes = {
        case GET at "/api/heartbeat" => Ok("Ok")
      }
    )
      // Attach a board to the server
      .attach(board)
      // Launch the server at port 8080
      .apply(8080)
  }
}
