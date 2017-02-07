package com.criteo.slab.app

import com.criteo.slab.core.{Board, Layout, ValueStore}
import com.criteo.slab.utils.Jsonable._
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller

class SlabController(
                      val settings: Seq[(Board, Layout)]
                    )(implicit valueStore: ValueStore) extends Controller {

  private val settingsMap: Map[String, (Board, Layout)] = settings.foldLeft(Map.empty[String, (Board, Layout)]) {
    case (acc, (board, layout)) => acc + (board.title -> (board, layout))
  }

  // APIs
  get("/api/boards/:board") { req: Request =>
    val board = for {
      raw <- req.params.get("board")
      boardTitle = java.net.URLDecoder.decode(raw, "UTF-8")
      board <- settingsMap.get(boardTitle).map(_._1)
    } yield board

    board.fold(response.notFound(s"board requested does not exist").toFuture) {
      _.apply(None)
        .map(_.toJSON)
        .map(response.ok.json)
    }
  }

  get("/api/layouts/:board") { req: Request =>
    val layout = for {
      raw <- req.params.get("board")
      boardTitle = java.net.URLDecoder.decode(raw, "UTF-8")
      layout <- settingsMap.get(boardTitle).map(_._2)
    } yield layout
    layout.fold(response.notFound("board requested does not exist").toFuture) { layout =>
      response.ok.json(layout.toJSON).toFuture
    }
  }

  // Static
  get("/:*") { req: Request =>
    val path = req.params("*")
    if (path.startsWith("api"))
      response.notFound(s"/$path not found")
    else
      response.ok.fileOrIndex(
        path,
        "index.html"
      )
  }
}
