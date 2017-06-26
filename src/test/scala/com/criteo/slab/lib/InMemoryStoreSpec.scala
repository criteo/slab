package com.criteo.slab.lib

import java.time.Instant
import java.time.temporal.ChronoUnit

import org.scalatest.{FlatSpec, Matchers}
import org.slf4j.LoggerFactory

import scala.collection.concurrent.TrieMap

class InMemoryStoreSpec extends FlatSpec with Matchers {
  val logger = LoggerFactory.getLogger(this.getClass)
  "Cleaner" should "remove expired entries" in {
    val cache = TrieMap.empty[(String, Long), Any]
    val cleaner = InMemoryStore.createCleaner(cache, 1, logger)

    cache += ("a", Instant.now.minus(2, ChronoUnit.DAYS).toEpochMilli) -> 1
    cache += ("b", Instant.now.minus(1, ChronoUnit.DAYS).toEpochMilli) -> 2
    cache += ("c", Instant.now.toEpochMilli) -> 3
    cleaner.run()
    cache.size shouldBe 1
    cache.head._1._1 shouldBe "c"
  }

}
