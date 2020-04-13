package com.moda

import software.amazon.awssdk.regions.Region
import io.circe.Decoder

import scala.io.Source

package object consumer {

  import cats.data.NonEmptyList

  import io.circe.parser._

  final case class StreamConfig(
    streamName: String,
    roleArnOpt: Option[String] = None,
    regionOpt: Option[Region] = None
  )
  implicit val streamConfigDecoder: Decoder[StreamConfig] = Decoder.instance(
    w =>
      for {
        streamName <- w.downField("stream_name").as[String]
        roleArnOpt <- w.downField("role_arn").as[Option[String]]
        regionOpt  <- w.downField("region").as[Option[String]]
      } yield StreamConfig(streamName, roleArnOpt, regionOpt.map(Region.of))
  )

  final case class ConsumerConfig(
    streamConfigs: NonEmptyList[StreamConfig]
  )
  implicit val consumerConfigDecoder: Decoder[ConsumerConfig] = Decoder.instance(
    w =>
      for {
        streamConfigs <- w.downField("streams").as[List[StreamConfig]]
      } yield ConsumerConfig(NonEmptyList.fromListUnsafe(streamConfigs))
  )

  def parseJson(jsonString: String): ConsumerConfig =
    decode[ConsumerConfig](jsonString) match {
      case Left(error)  => sys.error(s"Failed to parse JSON, $error")
      case Right(value) => value
    }

  def parseFile(fileName: String): ConsumerConfig = {
    val source = Source.fromFile(fileName)
    val json = try source.getLines().mkString
    finally source.close()
    parseJson(json)
  }
}
