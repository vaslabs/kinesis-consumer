package com.moda.consumer.kinesis

import cats.effect.ConcurrentEffect
import fs2.aws.kinesis.CommittableRecord
import fs2.Pipe
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger

trait RecordProcessor {
  def processEvents[F[_]: ConcurrentEffect](
    streamName: String
  )(implicit logger: SelfAwareStructuredLogger[F]): Pipe[F, CommittableRecord, CommittableRecord] =
    _.evalMap(eventToRecordAndJson)
      .through(logParsingErrors)
      .through(logEvents(streamName))
}

object RecordProcessor extends RecordProcessor
