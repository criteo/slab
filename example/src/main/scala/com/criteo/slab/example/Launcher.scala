// Example: A Slab server
//
// Guide for creating a Slab server
package com.criteo.slab.example

import java.net.URLDecoder

import cats.effect.IO
import com.criteo.slab.app.StateService.NotFoundError
import com.criteo.slab.app.WebServer
import com.criteo.slab.lib.InMemoryStore
import lol.http._
import org.slf4j.LoggerFactory

object Launcher {

  import SimpleBoard._

  import scala.concurrent.ExecutionContext.Implicits.global

  private val logger = LoggerFactory.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {
    require(args.length == 1, "you must supply a port!")
    val port = args(0).toInt
    // You should provide codec for checked value types for values to be persistent in a store
    import InMemoryStore.codec
    // Define a value store for uploading and restoring history
    implicit val store = new InMemoryStore
    // Create a web server
    WebServer()
      // You can define custom routes, Slab web server is built with [lolhttp](https://github.com/criteo/lolhttp)
      .withRoutes(stateService => {
        case GET at "/api/heartbeat" => Ok("ok")
        case GET at url"/api/boards/$board/status" =>
          IO.fromFuture(IO(
            stateService
              .current(URLDecoder.decode(board, "UTF-8")).map(view => Ok(view.status.name))
              .recover {
                case NotFoundError(message) => NotFound(message)
                case e =>
                  logger.error(e.getMessage, e)
                  InternalServerError
              }
          ))
      })
      // Attach a board to the server
      .attach(board)
      // Launch the server at port
      .apply(port)
  }
}
