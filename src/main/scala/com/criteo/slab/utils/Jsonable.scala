package com.criteo.slab.utils

import org.json4s.native.Serialization
import org.json4s.{DefaultFormats, Formats, Serializer}

import scala.util.Try

/** A type class that enables JSON serialization/deserialization
  *
  * @tparam T The underlying type
  */
private[slab] trait Jsonable[T] {
  val serializers: Seq[Serializer[_]] = List.empty
}

private[slab] object Jsonable {

  def apply[T: Jsonable]() = implicitly[Jsonable[T]]

  def parse[T: Manifest](in: String, formats: Formats = DefaultFormats): Try[T] = {
    Try(Serialization.read[T](in)(formats, implicitly[Manifest[T]]))
  }

  type Id[T <: AnyRef] = T

  class ToJsonable[C[_ <: AnyRef] <: AnyRef, T <: AnyRef : Jsonable](in: C[T]) {
    implicit val formats = DefaultFormats ++ Jsonable[T].serializers
    def toJSON: String = Serialization.write(in)
  }

  implicit class ToJsonPlain[T <: AnyRef: Jsonable](in: T) extends ToJsonable[Id, T](in)
  implicit class ToJsonMap[M[_, _] <: Map[_, _], K, T <: AnyRef: Jsonable](in: M[K, T]) extends ToJsonable[({type l[A] = M[K, A]})#l, T](in)
  implicit class ToJsonSeq[S[_] <: Seq[_], T <: AnyRef: Jsonable](in: S[T]) extends ToJsonable[S, T](in)
}
