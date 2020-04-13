package com.moda.consumer

import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import fs2.Pipe
import fs2.aws.kinesis.CommittableRecord
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.circe.Json
import io.circe.jawn.CirceSupportParser

package object kinesis {
  type Payload = (CommittableRecord, Json)
  def eventToRecordAndJson[F[_]: Sync]: CommittableRecord => F[Either[Throwable, Payload]] =
    cr =>
      Sync[F].delay(
        CirceSupportParser
          .parseFromByteBuffer(cr.record.data())
          .toEither
          .map { json =>
            cr -> json
          }
      )

  def logParsingErrors[F[_]: Applicative, T](
    implicit logger: SelfAwareStructuredLogger[F]
  ): Pipe[F, Either[Throwable, T], T] = {
    def logError: Either[Throwable, T] => F[Option[T]] = {
      case Right(event) => Applicative[F].pure(Some(event))
      case Left(error)  => logger.error(error)("Failed to parse Json from record.") *> Applicative[F].pure(None)
    }

    _.through(_.evalMap(logError)).unNone
  }

  def logEvents[F[_]: Applicative](
    streamName: String
  )(implicit logger: SelfAwareStructuredLogger[F]): Pipe[F, Payload, CommittableRecord] =
    _.evalMap {
      case (cr, event) =>
        logger.info(s"[$streamName] $event") *> Applicative[F].pure(cr)
    }
}
