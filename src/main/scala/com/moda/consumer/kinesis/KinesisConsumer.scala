package com.moda.consumer.kinesis

import cats.effect.{ ConcurrentEffect, ContextShift, Timer }
import fs2.Pipe
import fs2.aws.kinesis._
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient
import software.amazon.kinesis.retrieval.KinesisClientRecord

trait KinesisConsumer[F[_]] {
  def mkStream: fs2.Stream[F, CommittableRecord]
  def mkCheckpointer: fs2.Pipe[F, CommittableRecord, KinesisClientRecord]
}

object KinesisConsumer {
  class DefaultKinesisConsumer[F[_]: ConcurrentEffect: ContextShift: Timer](
    kinesisSettings: KinesisConsumerSettings,
    kinesisCheckpointSettings: KinesisCheckpointSettings,
    kinesisAsyncClient: KinesisAsyncClient
  ) extends KinesisConsumer[F] {
    override def mkStream: fs2.Stream[F, CommittableRecord] =
      consumer.readFromKinesisStream[F](kinesisSettings, kinesisAsyncClient)

    override def mkCheckpointer: Pipe[F, CommittableRecord, KinesisClientRecord] =
      consumer.checkpointRecords[F](kinesisCheckpointSettings)
  }
}
