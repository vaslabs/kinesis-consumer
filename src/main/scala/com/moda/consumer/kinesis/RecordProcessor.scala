package com.moda.consumer.kinesis

import cats.effect.ConcurrentEffect
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import software.amazon.kinesis.retrieval.KinesisClientRecord

class RecordProcessor[F[_]: ConcurrentEffect](streamName: String, kinesisConsumer: KinesisConsumer[F]) {

  def createStream(implicit logger: SelfAwareStructuredLogger[F]): fs2.Stream[F, KinesisClientRecord] =
    kinesisConsumer.mkStream
      .evalMap(eventToRecordAndJson)
      .through(logParsingErrors)
      .through(logEvents(streamName))
      .through(kinesisConsumer.mkCheckpointer)
}
