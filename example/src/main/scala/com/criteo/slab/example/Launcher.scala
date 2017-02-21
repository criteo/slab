package com.criteo.slab.example

import com.criteo.slab.app.WebServer

import scala.concurrent.ExecutionContext.Implicits.global
object Launcher {
  import Boards._
  def main(args: Array[String]): Unit = {
    new WebServer(List(simpleBoard)).apply(8081)
  }
}
