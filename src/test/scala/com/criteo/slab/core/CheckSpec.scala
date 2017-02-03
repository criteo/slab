package com.criteo.slab.core

import com.criteo.slab.helper.TwitterFutures
import com.twitter.util.Future
import org.joda.time.DateTime
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}
import org.mockito.Mockito._

class CheckSpec extends FlatSpec with Matchers with TwitterFutures {

  implicit val store = spy(TestStore)

  "now" should "call apply and upload, return the current view" in {
    whenReady(versionCheck.now.toFutureConcept) { res =>
      verify(store).upload("app.version", Seq(("version" -> 9000)))
      res shouldEqual ViewLeaf("app version", View(Status.Success, "version 9000"))
    }
  }

  "replay" should "fetch value from store, return a view corresponding to the context" in {
    val context = Context(new DateTime(100))
    whenReady(versionCheck.replay(context).toFutureConcept) { res =>
      verify(store).fetch("app.version", context)
      res shouldEqual ViewLeaf("app version", View(Status.Success, "version 100"))
    }
  }

}
