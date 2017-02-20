package com.criteo.slab.core

import com.criteo.slab.helper.FutureTests
import org.joda.time.DateTime
import org.mockito.Mockito._
import org.scalatest.{FlatSpec, Matchers}

class CheckSpec extends FlatSpec with Matchers with FutureTests {

  implicit val store = spy(TestStore)

  "now" should "call apply and upload, return the current view" in {
    whenReady(versionCheck.now) { res =>
      verify(store).upload("app.version", Seq(("version" -> 9000)))
      res shouldEqual ViewLeaf("app version", View(Status.Success, "version 9000"))
    }
  }

  "replay" should "fetch value from store, return a view corresponding to the context" in {
    val context = Context(new DateTime(100))
    whenReady(versionCheck.replay(context)) { res =>
      verify(store).fetch("app.version", context)
      res shouldEqual ViewLeaf("app version", View(Status.Success, "version 100"))
    }
  }

}
