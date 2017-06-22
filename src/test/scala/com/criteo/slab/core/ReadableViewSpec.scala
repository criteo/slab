package com.criteo.slab.core

import com.criteo.slab.core.{BoardView, BoxView, CheckView, ReadableView}
import com.criteo.slab.utils.Jsonable._
import org.scalatest.{FlatSpec, Matchers}

class ReadableViewSpec extends FlatSpec with Matchers {

  "toJSON" should "work" in {
    val boardView: ReadableView = BoardView(
      "board1",
      Status.Success,
      "msg",
      List(
        BoxView("box1", Status.Success, "msg", List(
          CheckView("check1", Status.Warning, "msg"),
          CheckView("check2", Status.Error, "msg", Some("label"))
        ))
      )
    )
    boardView.toJSON shouldEqual
      """
        |{
        |"title":"board1",
        |"status":"SUCCESS",
        |"message":"msg",
        |"boxes":[{
        |"title":"box1",
        |"status":"SUCCESS",
        |"message":"msg",
        |"checks":[
        |{"title":"check1","status":"WARNING","message":"msg"},
        |{"title":"check2","status":"ERROR","message":"msg","label":"label"}
        |]}
        |]}""".stripMargin.replaceAll("\n", "")
  }
}
