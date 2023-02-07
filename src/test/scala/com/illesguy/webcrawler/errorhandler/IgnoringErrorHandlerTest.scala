package com.illesguy.webcrawler.errorhandler

import org.jsoup.{HttpStatusException, UnsupportedMimeTypeException}
import org.scalatest.{FlatSpec, Matchers}
import org.scalatestplus.mockito.MockitoSugar

import scala.util.{Failure, Try}

class IgnoringErrorHandlerTest extends FlatSpec with Matchers with MockitoSugar {

  "IgnoringErrorHandler" should "return empty list for http status error" in {
    val result = IgnoringErrorHandler.handleError(new HttpStatusException("Not found!", 404, "url"), "url")
    result should be (Seq())
  }

  it should "return empty list for unsupported mime type error" in {
    val result = IgnoringErrorHandler.handleError(new UnsupportedMimeTypeException("Invalid mime type!", "image/jpeg", "url"), "url")
    result should be (Seq())
  }

  it should "throw exception when it is of an unhandled type" in {
    val exception = new RuntimeException("Unexpected error occured!")

    val result = Try(IgnoringErrorHandler.handleError(exception, "url"))

    result should be (Failure(exception))
  }
}
