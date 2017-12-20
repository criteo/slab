package com.criteo.slab

import java.time.Instant

import com.criteo.slab.lib.Values.Slo
import shapeless.HNil

import scala.concurrent.Future
import scala.util.Try
import org.mockito.Mockito._

package object core {
  val check1 = Check[String](
    "c.1",
    "check 1",
    () => Future.successful("new value"),
    (value, _) => View(Status.Success, s"check 1 message: $value")
  )
  val check2 = Check[String](
    "c.2",
    "check 2",
    () => Future.successful("new value"),
    (value, _) => View(Status.Success, s"check 2 message: $value")
  )
  val check3 = Check[Int](
    "c.3",
    "check 3",
    () => Future.successful(3),
    (value, _) => View(Status.Success, s"check 3 message: $value")
  )
  val spiedCheck1 = spy(check1)
  val spiedCheck2 = spy(check2)
  val spiedCheck3 = spy(check3)

  val box1 = Box[String](
    "box 1",
    List(spiedCheck1, spiedCheck2),
    (views, _) => views.get(spiedCheck2).map(_.view.copy(message = "box 1 message")).getOrElse(View(Status.Unknown, "unknown"))
  )

  val box2 = Box[Int](
    "box 2",
    List(spiedCheck3),
    (views, _) => views.values.head.view.copy(message = "box 2 message")
  )

  val board = Board(
    "board",
    box1 :: box2 :: HNil,
    (_, _) => View(Status.Unknown, "board message"),
    Layout(
      Column(
        50,
        Row("r1", 100, List(box1))
      ),
      Column(
        50,
        Row("r2", 100, List(box2))
      )
    )
  )


  implicit def codecInt = new Codec[Int, String] {

    override def encode(v: Int): String = v.toString

    override def decode(v: String): Try[Int] = Try(v.toInt)
  }

  implicit def codecSlo = new Codec[Slo, String] {

    override def encode(v: Slo): String = v.underlying.toString

    override def decode(v: String): Try[Slo] = Try(Slo(v.toDouble))
  }

  implicit def codecString = new Codec[String, String] {
    override def encode(v: String): String = v

    override def decode(v: String): Try[String] = Try(v)
  }

  implicit def store = new Store[String] {
    override def upload[T](id: String, context: Context, v: T)(implicit ev: Codec[T, String]): Future[Unit] = Future.successful(())

    override def fetch[T](id: String, context: Context)(implicit ev: Codec[T, String]): Future[Option[T]] = Future.successful(ev.decode("100").toOption)

    override def fetchHistory[T](id: String, from: Instant, until: Instant)(implicit ev: Codec[T, String]): Future[Seq[(Long, T)]] = Future.successful(List.empty)

    override def uploadSlo(id: String, context: Context, v: Slo)(implicit codec: Codec[Slo, String]): Future[Unit] = Future.successful(())

    def fetchSloHistory(id: String, from: Instant, until: Instant)(implicit codec: Codec[Slo, String]): Future[Seq[(Long, Slo)]] = Future.successful(List.empty)
  }
}
