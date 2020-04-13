package com.moda.consumer

import cats.data.NonEmptyList
import scopt.OptionParser
import software.amazon.awssdk.regions.Region

final case class Parameters(
  streamNameOpt: Option[String] = None,
  roleArnOpt: Option[String] = None,
  regionOpt: Option[String] = None,
  fileNameOpt: Option[String] = None,
  jsonOpt: Option[String] = None
) {

  def isValid: Either[String, Unit] = {
    import Parameters._
    if (streamNameOpt.nonEmpty) {
      if (fileNameOpt.nonEmpty || jsonOpt.nonEmpty)
        Left(Error_One_Stream_Only)
      else Right(())
    } else {
      if (roleArnOpt.nonEmpty || regionOpt.nonEmpty)
        Left(Error_Only_With_Stream)
      else if (fileNameOpt.nonEmpty) {
        if (jsonOpt.nonEmpty) Left(Error_Filename_Only) else Right(())
      } else {
        if (jsonOpt.isEmpty) Left(Error_Need_Stream) else Right(())
      }
    }
  }

  def toConfig: ConsumerConfig =
    if (streamNameOpt.nonEmpty) {
      ConsumerConfig(
        NonEmptyList.of(
          StreamConfig(streamNameOpt.get, roleArnOpt, regionOpt.map(Region.of))
        )
      )
    } else {
      if (jsonOpt.nonEmpty) parseJson(jsonOpt.get)
      else parseFile(fileNameOpt.get)
    }
}

object Parameters {
  private[consumer] val Error_One_Stream_Only  = "Must not specify file or json, when a stream is provided."
  private[consumer] val Error_Only_With_Stream = "Must not specify role or region, when a stream is not provided."
  private[consumer] val Error_Filename_Only    = "Must not specify json, when a file name is provided."
  private[consumer] val Error_Need_Stream      = "Must specify at least one stream to consume."

  private[this] def parser(appName: String, versionStr: String): OptionParser[Parameters] =
    new scopt.OptionParser[Parameters](appName) {
      head(appName, versionStr)

      opt[String]('s', "stream")
        .valueName("<stream>")
        .text("(Optional) The name of the Kinesis stream to consume events from.")
        .action { (value, params) =>
          params.copy(streamNameOpt = Some(value))
        }

      opt[String]('r', "role")
        .valueName("<role>")
        .text("(Optional) The IAM role to assume before consuming from Kinesis.")
        .action { (value, params) =>
          params.copy(roleArnOpt = Some(value))
        }

      opt[String]('g', "region")
        .valueName("<region>")
        .text("(Optional) The name of the region where the Kinesis stream is defined.")
        .action { (value, params) =>
          params.copy(regionOpt = Some(value))
        }

      opt[String]('f', "file")
        .valueName("<file>")
        .text("(Optional) A file to read streams configuration from.")
        .action { (value, params) =>
          params.copy(fileNameOpt = Some(value))
        }

      opt[String]('j', "json")
        .valueName("<json>")
        .text("(Optional) The streams configuration in JSON format.")
        .action { (value, params) =>
          params.copy(jsonOpt = Some(value))
        }

      checkConfig { params =>
        params.isValid match {
          case Left(message) => failure(message)
          case Right(_)      => success
        }
      }

      note(
        "For consuming a single stream, use --stream and optionally --role and --region.\n" +
          "For consuming one or more streams, use either --json or --file to provide all the configuration.\n" +
          "See the manual for the JSON format required."
      )

      help('h', "help").text("Prints this usage text.")
    }

  def withConfig[T](args: List[String])(body: ConsumerConfig => T)(err: =>T): T =
    parser(BuildInfo.name, BuildInfo.version).parse(args, Parameters()) match {
      case Some(parameters) => body(parameters.toConfig)
      case None             => err
    }
}
