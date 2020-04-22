package com.moda.consumer.kinesis

import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import cats.implicits._
import com.moda.consumer.kinesis.KinesisConsumer.DefaultKinesisConsumer
import com.moda.consumer.{StreamConfig, kinesis}
import fs2.Stream
import fs2.aws.kinesis.{KinesisCheckpointSettings, KinesisConsumerSettings}
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import software.amazon.awssdk.auth.credentials.{AwsCredentialsProvider, DefaultCredentialsProvider}
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient
import software.amazon.awssdk.services.sts.StsClient
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest
import software.amazon.kinesis.common.InitialPositionInStream
import software.amazon.kinesis.retrieval.KinesisClientRecord

import scala.concurrent.duration._

trait KinesisStream {

  def createStream[F[_]: ConcurrentEffect: ContextShift: Timer](
    appName: String,
    sts: StsClient,
    streamConfig: StreamConfig
  )(implicit logger: SelfAwareStructuredLogger[F]): F[Stream[F, KinesisClientRecord]] =
    for {
      kinesisCheckpointSettings <- KinesisCheckpointSettings(Int.MaxValue, 10.seconds).liftTo[F]

      sessionName = s"$appName-${streamConfig.streamName}"

      kinesisConsumerSettings <- KinesisConsumerSettings(
                                  streamName = streamConfig.streamName,
                                  appName = sessionName,
                                  maxConcurrency = Int.MaxValue,
                                  bufferSize = 50000,
                                  initialPositionInStream = Left(InitialPositionInStream.TRIM_HORIZON)
                                ).liftTo[F]
    } yield {
      new kinesis.RecordProcessor[F](
        streamConfig.streamName,
        new DefaultKinesisConsumer[F](
          kinesisConsumerSettings,
          kinesisCheckpointSettings,
          createKinesisClient(kinesisConsumerSettings, sts, sessionName, streamConfig)
        )
      ).createStream
    }

  private[this] def createKinesisClient(
    settings: KinesisConsumerSettings,
    client: StsClient,
    sessionName: String,
    streamConfig: StreamConfig
  ): KinesisAsyncClient =
    KinesisAsyncClient
      .builder()
      .credentialsProvider(createCredentialsProvider(streamConfig.roleArnOpt, sessionName, client))
      .region(streamConfig.regionOpt.getOrElse(Region.US_EAST_1))
      .httpClientBuilder(NettyNioAsyncHttpClient.builder().maxConcurrency(settings.maxConcurrency))
      .build()

  private[this] def createCredentialsProvider(
    roleArnOpt: Option[String],
    sessionName: String,
    client: StsClient
  ): AwsCredentialsProvider =
    roleArnOpt.fold(DefaultCredentialsProvider.builder().build().asInstanceOf[AwsCredentialsProvider]) { roleArn =>
      StsAssumeRoleCredentialsProvider
        .builder()
        .stsClient(client)
        .refreshRequest(
          AssumeRoleRequest
            .builder()
            .roleArn(roleArn)
            .roleSessionName(sessionName)
            .build()
        )
        .build()
        .asInstanceOf[AwsCredentialsProvider]
    }
}

object KinesisStream extends KinesisStream
