package com.criteo.slab.core

import com.criteo.slab.utils.Jsonable
import org.json4s.JsonAST.JString
import org.json4s.{CustomSerializer, Serializer}

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

  implicit object ToJSON extends Jsonable[Status] {
    override val serializers: Seq[Serializer[_]] = List(Ser)

    object Ser extends CustomSerializer[Status](_ => (
      {
        case JString(status) => Status.from(status)
      },
      {
        case s: Status => JString(s.name)
      }
    ))

  }
}
