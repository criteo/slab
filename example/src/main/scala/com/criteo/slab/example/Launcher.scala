package com.criteo.slab.example

import com.criteo.slab.app.WebServer

import scala.concurrent.ExecutionContext.Implicits.global
object Launcher {
  def main(args: Array[String]): Unit = {
    new WebServer(List(SimpleBoard())).apply(8081)
  }
}
