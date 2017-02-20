package com.criteo.slab.helper

import org.scalatest.concurrent.{Futures, ScalaFutures}

trait FutureTests extends Futures with ScalaFutures {
  implicit val ec = concurrent.ExecutionContext.Implicits.global
}

object FutureTests extends FutureTests