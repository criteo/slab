package com.criteo.slab.lib

/**
  * Predefined value types that work with a Graphite store
  */
object Values {

  /**
    * A value representing a version number
    *
    * @param underlying The version
    */
  case class Version(val underlying: Double) extends AnyVal

  /**
    * A value representing a latency
    *
    * @param underlying The latency value
    */
  case class Latency(val underlying: Long) extends AnyVal

  /**
    * A value representing a SLO number
    *
    * @param underlying The SLO
    */
  case class Slo(val underlying: Double) extends AnyVal
}
