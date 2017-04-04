import org.joda.time.DateTime

import scala.collection.concurrent.TrieMap
import scala.util.Random

DateTime.now
  .withSecondOfMinute(0)
  .withMillisOfSecond(0)
.toString()

val map = TrieMap.empty[String, TrieMap[String, Double]]

map += ("a" -> TrieMap.empty)

map.get("a").map(_ += "a" -> 2)

map --= List("a")

Random.nextInt(1000)
Random.nextInt(1000)
Random.nextDouble()*1000