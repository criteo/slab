package com.criteo.slab.utils

import org.json4s.native.Serialization
import org.json4s.{DefaultFormats, Formats, Serializer}

import scala.util.Try

trait Jsonable[T] {
  val serializers: Seq[Serializer[_]] = List.empty
}

object Jsonable {

  def apply[T: Jsonable]() = implicitly[Jsonable[T]]

  implicit class ToJson[T <: AnyRef : Jsonable](in: T) {
    implicit val formats = DefaultFormats ++ implicitly[Jsonable[T]].serializers

    def toJSON: String = Serialization.write(in)
  }

  implicit class ToJsonSeq[S[_] <: Seq[_], T <: AnyRef : Jsonable](in: S[T]) {
    implicit val formats = DefaultFormats ++ implicitly[Jsonable[T]].serializers

    def toJSON: String = Serialization.write(in)
  }

  implicit class ToJsonMap[M[_, _] <: Map[_, _], K, T <: AnyRef : Jsonable](in: M[K, T]) {
    implicit val formats = DefaultFormats ++ implicitly[Jsonable[T]].serializers

    def toJSON: String = Serialization.write(in)
  }

  def parse[T: Manifest](in: String, formats: Formats = DefaultFormats): Try[T] = {
    Try(Serialization.read[T](in)(formats, implicitly[Manifest[T]]))
  }
}
