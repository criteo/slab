package com.criteo.slab.utils

import org.json4s.native.Serialization
import org.json4s.{DefaultFormats, Formats, Serializer}

import scala.util.Try

trait Jsonable[T] {
  val serializers: Seq[Serializer[_]]
}

object Jsonable {

  def apply[T: Jsonable]() = implicitly[Jsonable[T]]

  implicit class ToJson[T <: AnyRef : Jsonable](in: T) {
    implicit val formats = DefaultFormats ++ implicitly[Jsonable[T]].serializers

    def toJSON: String = Serialization.write(in)
  }

  implicit val stringList = new Jsonable[Iterable[String]] {
    override val serializers: Seq[Serializer[_]] = Seq.empty
  }

  def constructCol[Col[_], T: Jsonable]() = new Jsonable[Col[T]] {
    override val serializers: Seq[Serializer[_]] = implicitly[Jsonable[T]].serializers
  }

  def constructMap[K, V: Jsonable, MapLike[_, _]]() = new Jsonable[MapLike[K, V]] {
    override val serializers: Seq[Serializer[_]] = implicitly[Jsonable[V]].serializers
  }

  def parse[T: Manifest](in: String, formats: Formats = DefaultFormats): Try[T] = {
    Try(Serialization.read[T](in)(formats, implicitly[Manifest[T]]))
  }
}
