package com.moda.consumer

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import software.amazon.awssdk.regions.Region

class ConsumerConfigSpec extends AnyWordSpec with Matchers {

  private[this] val streams = List(
    StreamConfig(streamName = "one"),
    StreamConfig(streamName = "two", roleArnOpt = Some("role-for-two")),
    StreamConfig(streamName = "three", regionOpt = Some(Region.US_EAST_1)),
    StreamConfig(streamName = "four", regionOpt = Some(Region.US_WEST_1), roleArnOpt = Some("role-for-four"))
  )

  "parseJson" should {
    "throw an exception if invalid JSON" in {
      assertThrows[RuntimeException] {
        parseJson("Boom!")
      }
    }

    "parse a valid JSON" in {
      parseJson("""
          | {
          |   "streams": [
          |     {
          |       "stream_name": "one"
          |     },
          |     {
          |       "stream_name": "two",
          |       "role_arn": "role-for-two"
          |     },
          |     {
          |       "stream_name": "three",
          |       "region": "us-east-1"
          |     },
          |     {
          |       "stream_name": "four",
          |       "region": "us-west-1",
          |       "role_arn": "role-for-four"
          |     }
          |   ]
          | }
          |""".stripMargin).streamConfigs.toList must contain theSameElementsAs streams
    }
  }

  "parseFile" should {
    "parse a file correctly" in {
      parseFile("src/test/resources/example.json").streamConfigs.toList must contain theSameElementsAs streams
    }
  }
}
