package com.criteo.slab.utils

import org.json4s.native.Serialization
import org.json4s.{DefaultFormats, Serializer}

trait Jsonable[T] {
  val serializers: Seq[Serializer[_]]
}

object Jsonable {

  implicit class ToJson[T <: AnyRef : Jsonable](in: T) {
    implicit val formats = DefaultFormats ++ implicitly[Jsonable[T]].serializers

    def toJSON: String = Serialization.write(in)
  }

}
