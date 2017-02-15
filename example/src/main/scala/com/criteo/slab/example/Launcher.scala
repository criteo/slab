package com.criteo.slab.example

import com.criteo.slab.app.WebServer

object Launcher {
  import Boards._
  def main(args: Array[String]): Unit = {
    new WebServer(List(simpleBoard)).main(args)
  }
}
