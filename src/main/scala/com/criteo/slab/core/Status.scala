package com.criteo.slab.core

sealed class Status(val name: String, val level: Int) extends Ordered[Status] {
  override def compare(that: Status) = this.level.compare(that.level)
}

object Status {
  case object Success extends Status("SUCCESS", 0)
  case object Warning extends Status("WARNING", 1)
  case object Error extends Status("ERROR", 2)
  case object Unknown extends Status("UNKNOWN", 3)

  def from(in: String) = in.toUpperCase match {
    case "SUCCESS" => Success
    case "WARNING" => Warning
    case "ERROR" => Error
    case "UNKNOWN" => Unknown
  }
}
