package com.criteo.slab.core

import java.time.Instant
import java.time.temporal.ChronoUnit

import com.criteo.slab.core.Executor.{FetchBoardHistory, FetchBoardHourlySlo, RunBoard}
import com.criteo.slab.lib.Values.Slo
import org.slf4j.LoggerFactory
import shapeless.ops.hlist.{Mapper, ToTraversable, ZipConst}
import shapeless.poly.Case3
import shapeless.{HList, Poly1, Poly3}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Executes the logic of a board
  *
  * @param board    The Board
  * @param runBoard Function for executions
  * @tparam L
  */
private[slab] case class Executor[L <: HList](board: Board[L])(
  implicit
  runBoard: Case3.Aux[RunBoard.type, Board[L], Context, Boolean, Future[BoardView]],
  fetchBoardHistory: Case3.Aux[FetchBoardHistory.type, Board[L], Instant, Instant, Future[Seq[(Long, BoardView)]]],
  fetchBoardHourlySlo: Case3.Aux[FetchBoardHourlySlo.type, Board[L], Instant, Instant, Future[Seq[(Long, Double)]]]
) {
  def apply(ctx: Option[Context] = None): Future[BoardView] = {
    RunBoard(board, ctx.getOrElse(Context.now), ctx.isDefined)
  }

  def fetchHistory(from: Instant, until: Instant): Future[Seq[(Long, BoardView)]] = {
    FetchBoardHistory(board, from, until)
  }

  def fetchHourlySlo(from: Instant, until: Instant): Future[Seq[(Long, Double)]] = {
    FetchBoardHourlySlo(board, from, until)
  }

}

private[slab] object Executor {
  private val logger = LoggerFactory.getLogger(this.getClass)

  object RunBoard extends Poly3 {
    implicit def f[L <: HList, A <: HList, B <: HList, Repr](
                                                        implicit
                                                        zip: ZipConst.Aux[(Context, Boolean), L, A],
                                                        mapper: Mapper.Aux[RunBox.type, A, B],
                                                        to: ToTraversable.Aux[B, List, Future[(Box[_], BoxView)]],
                                                        ec: ExecutionContext,
                                                        store: Store[Repr],
                                                        codec: Codec[Slo, Repr]
                                                      ): Case.Aux[Board[L], Context, Boolean, Future[BoardView]] =
      at { (board, context, isReplay) =>
        Future.sequence {
          board.boxes.zipConst(context -> isReplay).map(RunBox).toList[Future[(Box[_], BoxView)]]
        } flatMap { pairs =>
          val bv = pairs.toMap
          val aggView = board.aggregate(bv.mapValues(_.toView), context)
          val boardView = BoardView(board.title, aggView.status, aggView.message, bv.values.toList)
          if (!isReplay) {
            val slo = if (boardView.status == Status.Error) Slo(0) else Slo(1)
            store.uploadSlo(board.title, context, slo)
              .map(_ => boardView)
              .recover { case e =>
                logger.error(e.getMessage, e)
                boardView
              }
          } else {
            Future.successful(boardView)
          }
        }
      }
  }

  object RunBox extends Poly1 {
    implicit def f[T, Repr](
                             implicit
                             ec: ExecutionContext,
                             store: Store[Repr],
                             codec: Codec[T, Repr]
                           ): Case.Aux[(Box[T], (Context, Boolean)), Future[(Box[T], BoxView)]] =
      at { case (box, (context, isReplay)) =>
        Future.sequence {
          if (isReplay)
            box.checks.map(replayCheck(_, context))
          else
            box.checks.map(runCheck(_, context))
        } map { pairs =>
          val aggView = box.aggregate(
            pairs.map { case (check, (checkView, value)) => check -> CheckResult(checkView.toView, value) }.toMap,
            context
          )
          (box, BoxView(box.title, aggView.status, aggView.message, pairs.map(_._2._1).toList))
        }
      }
  }

  def runCheck[T, Repr](check: Check[T], context: Context)(
    implicit
    store: Store[Repr],
    codec: Codec[T, Repr],
    ec: ExecutionContext
  ) = {
    check
      .apply()
      .flatMap(value =>
        store
          .upload(check.id, context, value)
          .map(_ => value)
          .recover { case e =>
            logger.error(e.getMessage, e)
            value
          }
      )
      .map(value => check.display(value, context) -> Some(value))
      .recover { case e =>
        logger.error(e.getMessage, e)
        View(Status.Unknown, e.getMessage) -> None
      }
      .map { case (view, maybeValue) =>
        (check, (CheckView(check.title, view.status, view.message, view.label), maybeValue))
      }
  }

  def replayCheck[T, Repr](check: Check[T], context: Context)(
    implicit
    store: Store[Repr],
    codec: Codec[T, Repr],
    ec: ExecutionContext
  ) = {
    store
      .fetch(check.id, context)
      .flatMap {
        case Some(v) => Future.successful(check.display(v, context) -> Some(v))
        case None => Future.failed(new NoSuchElementException(s"value of ${check.id} at ${context.when.toEpochMilli} is missing"))
      }
      .recover { case e =>
        logger.error(e.getMessage, e)
        View(Status.Unknown, e.getMessage) -> None
      }
      .map { case (view, maybeValue) =>
        (check, (CheckView(check.title, view.status, view.message, view.label), maybeValue))
      }
  }

  object FetchBoardHourlySlo extends Poly3 {
    private def avg(values: Seq[Slo]): Double = {
      val size = values.size
      if (size > 0) values.map(_.underlying).sum / size else 0
    }

    implicit def f[L <: HList, Repr](
                                      implicit
                                      ec: ExecutionContext,
                                      store: Store[Repr],
                                      codec: Codec[Slo, Repr]
                                    ): Case.Aux[Board[L], Instant, Instant, Future[Seq[(Long, Double)]]] =
      at { (board, from, until) =>
        store.fetchSloHistory(board.title, from, until).map { values =>
          values.map {
            case (ts, value) => Instant.ofEpochMilli(ts).truncatedTo(ChronoUnit.HOURS).toEpochMilli -> value
          }.groupBy(_._1).map {
            case (hour, list) => hour -> avg(list.map(_._2))
          }.toSeq
        }
      }
  }

  // History
  object FetchBoardHistory extends Poly3 {
    implicit def f[L <: HList, A <: HList, B <: HList](
                                                        implicit
                                                        zipConst: ZipConst.Aux[(Instant, Instant), L, A],
                                                        mapper: Mapper.Aux[FetchBoxHistory.type, A, B],
                                                        toTraversable: ToTraversable.Aux[B, List, Future[Box[_] Tuple2 Seq[(Long, BoxView)]]],
                                                        ec: ExecutionContext
                                                      ): Case.Aux[Board[L], Instant, Instant, Future[Seq[(Long, BoardView)]]] =
      at { (board, from, until) =>
        Future.sequence {
          board.boxes.zipConst((from, until)).map(FetchBoxHistory).toList[Future[Box[_] Tuple2 Seq[(Long, BoxView)]]]
        }.map { boxes =>
          boxes.flatMap { case (box, boxViews) =>
            boxViews.map { case (ts, view) =>
              (ts, box, view)
            }
          }.groupBy(_._1).map { case (ts, xs) =>
            val boxViews = xs.map { case (_, box, boxView) => (box, boxView) }
            val aggView = board.aggregate(
              boxViews.map { case (box, boxView) => box -> boxView.toView }.toMap,
              Context(Instant.ofEpochMilli(ts))
            )
            (ts, BoardView(board.title, aggView.status, aggView.message, boxViews.map(_._2)))
          }.toList
        }
      }
  }

  object FetchBoxHistory extends Poly1 {
    implicit def f[T, Repr](
                             implicit
                             store: Store[Repr],
                             codec: Codec[T, Repr],
                             ec: ExecutionContext
                           ): Case.Aux[Box[T] Tuple2 (Instant, Instant), Future[Box[T] Tuple2 Seq[(Long, BoxView)]]] =
      at { case (box, (from, until)) =>
        fetchBoxHistory(box, from, until)
      }
  }

  def fetchBoxHistory[T, Repr](box: Box[T], from: Instant, until: Instant)(
    implicit
    store: Store[Repr],
    codec: Codec[T, Repr],
    ec: ExecutionContext
  ) = {
    Future.sequence {
      box.checks.map(fetchCheckHistory(_, from, until))
    } map { checks =>
      box -> checks.flatMap { case (check, checkViews) =>
        checkViews.map { case (ts, (view, value)) =>
          (ts, (check, view -> Some(value)))
        }
      }.groupBy(_._1).map { case (ts, tuples) =>
        val checkViews = tuples.map(_._2)
        val aggView = box.aggregate(
          checkViews.map { case (check, (checkView, value)) => check -> CheckResult(checkView.toView, value) }.toMap,
          Context(Instant.ofEpochMilli(ts))
        )
        (ts, BoxView(box.title, aggView.status, aggView.message, checkViews.map(_._2._1)))
      }.toSeq
    }
  }

  def fetchCheckHistory[T, Repr](check: Check[T], from: Instant, until: Instant)(
    implicit
    store: Store[Repr],
    codec: Codec[T, Repr],
    ec: ExecutionContext
  ) = {
    store
      .fetchHistory[T](check.id, from, until)
      .map(series =>
        check -> series.map { case (ts, value) =>
          val view = check.display(value, Context(Instant.ofEpochMilli(ts)))
          (ts, CheckView(check.title, view.status, view.message, view.label) -> value)
        }
      )
  }
}
