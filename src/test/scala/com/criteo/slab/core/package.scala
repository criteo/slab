package com.criteo.slab

import java.time.Instant

import com.criteo.slab.core._
import shapeless.HNil

import scala.concurrent.Future
import scala.util.Try
import org.mockito.Mockito._

package object core {
  val check1 = Check[Int](
    "c.1",
    "check 1",
    () => Future.successful(1),
    (_, _) => View(Status.Success, "check 1 message")
  )
  val check2 = Check[String](
    "c.2",
    "check 2",
    () => Future.successful("new value"),
    (value, _) => View(Status.Success, "check 2 message: " + value)
  )
  val spiedCheck1 = spy(check1)
  val spiedCheck2 = spy(check2)

  val box = Box(
    "box 1",
    spiedCheck1::spiedCheck2::HNil,
    (views, _) => views.get(spiedCheck1).getOrElse(View(Status.Unknown, "unknown")).copy(message = "box 1 message")
  )

  val board = Board(
    "board",
    box::HNil,
    (_, _) => View(Status.Unknown, "board message"),
    Layout(
      List.empty
    )
  )


  implicit def codecInt = new Codec[Int, String] {

    override def encode(v: Int): String = v.toString

    override def decode(v: String): Try[Int] = Try(v.toInt)
  }

  implicit def codecString = new Codec[String, String] {
    override def encode(v: String): String = v

    override def decode(v: String): Try[String] = Try(v)
  }
  implicit def store = new Store[String] {
    override def upload[T](id: String, context: Context, v: T)(implicit ev: Codec[T, String]): Future[Unit] = Future.successful(())

    override def fetch[T](id: String, context: Context)(implicit ev: Codec[T, String]): Future[T] = Future.successful(ev.decode("100").get)

    override def fetchHistory[T](id: String, from: Instant, until: Instant)(implicit ev: Codec[T, String]): Future[Seq[(Long, T)]] = Future.successful(List.empty)
  }
}
