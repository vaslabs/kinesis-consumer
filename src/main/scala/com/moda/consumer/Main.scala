package com.moda.consumer

import java.util.UUID

import cats.effect.{ ExitCode, IO, IOApp }
import cats.implicits._
import com.moda.consumer.kinesis.KinesisStream
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import software.amazon.awssdk.services.sts.StsClient

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    Parameters.withConfig(args)(exec)(IO.pure(ExitCode.Error))

  private[this] def exec: ConsumerConfig => IO[ExitCode] = config => {
    implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLoggerFromName("application")
    val appName                                        = s"kinesis-consumer-${UUID.randomUUID().toString.take(4)}"
    val sts                                            = StsClient.builder().build()

    fs2.Stream
      .emits(config.streamConfigs.toList)
      .covary[IO]
      .evalMap { streamConfig =>
        KinesisStream.createStream[IO](appName, sts, streamConfig)
      }
      .parJoin(Int.MaxValue)
      .compile
      .drain
      .as(ExitCode.Success)
  }
}
