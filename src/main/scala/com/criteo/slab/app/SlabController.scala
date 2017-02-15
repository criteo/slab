package com.criteo.slab.app

import com.criteo.slab.core.{Board, ValueStore}
import com.criteo.slab.utils.Jsonable._
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller


class SlabController(
                      val boards: Seq[Board]
                    )(implicit valueStore: ValueStore) extends Controller {
  private val boardsMap: Map[String, Board] = boards.foldLeft(Map.empty[String, Board]) {
    case (acc, board) => acc + (board.title -> board)
  }

  // APIs
  get("/api/boards/:board") { req: Request =>
    val board = for {
      raw <- req.params.get("board")
      boardTitle = java.net.URLDecoder.decode(raw, "UTF-8")
      board <- boardsMap.get(boardTitle)
    } yield board

    board.fold(response.notFound(s"board requested does not exist").toFuture) { board =>
      board.apply(None)
        .map(view => BoardResponse(view, board.layout, board.links).toJSON)
        .map(response.ok.json)
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
