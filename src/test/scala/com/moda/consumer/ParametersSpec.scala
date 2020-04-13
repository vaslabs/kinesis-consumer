package com.moda.consumer

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ParametersSpec extends AnyWordSpec with Matchers {

  import Parameters._
  private val Given = Some("value")

  "isValid" should {
    "reject illegal combination of stream name and either file name or json" in {
      Parameters(streamNameOpt = Given, fileNameOpt = Given).isValid.left.get must be(Error_One_Stream_Only)
      Parameters(streamNameOpt = Given, jsonOpt = Given).isValid.left.get must     be(Error_One_Stream_Only)
    }

    "reject role or region parameters, when stream not provided" in {
      Parameters(roleArnOpt = Given, fileNameOpt = Given).isValid.left.get must be(Error_Only_With_Stream)
      Parameters(regionOpt = Given, fileNameOpt = Given).isValid.left.get must  be(Error_Only_With_Stream)
    }

    "reject illegal combination of file name and json" in {
      Parameters(fileNameOpt = Given, jsonOpt = Given).isValid.left.get must be(Error_Filename_Only)
    }

    "require at least one stream to be provided" in {
      Parameters().isValid.left.get must be(Error_Need_Stream)
    }

    "accept valid combinations of parameters" in {
      Parameters(streamNameOpt = Given).isValid.isRight must be(true)
      Parameters(fileNameOpt = Given).isValid.isRight must   be(true)
      Parameters(jsonOpt = Given).isValid.isRight must       be(true)
    }
  }
}
