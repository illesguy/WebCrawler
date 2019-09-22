package com.illesguy.webcrawler.errorhandler

import org.jsoup.{HttpStatusException, UnsupportedMimeTypeException}
import org.junit.runner.RunWith
import org.scalatest.{FlatSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner
import org.scalatestplus.mockito.MockitoSugar

import scala.util.{Failure, Try}

@RunWith(classOf[JUnitRunner])
class ErrorHandlerTest extends FlatSpec with Matchers with MockitoSugar {

  "LoggingErrorHandler" should "return list with single element containing url and http status error" in {
    val result = LoggingErrorHandler.handleError(new HttpStatusException("Not found!", 404, "url"), "url")
    result should be (Seq("url => 404: Not found!"))
  }

  it should "return list with single element containing url and unsupported mime type error" in {
    val result = LoggingErrorHandler.handleError(new UnsupportedMimeTypeException("Invalid mime type!", "image/jpeg", "url"), "url")
    result should be (Seq("url => image/jpeg: Invalid mime type!"))
  }

  it should "throw exception when it is of an unhandled type" in {
    val exception = new RuntimeException("Unexpected error occured!")

    val result = Try(LoggingErrorHandler.handleError(exception, "url"))

    result should be (Failure(exception))
  }

  "IgnoringErrorHandler" should "return an empty list regardless what exception is passed in to be handled" in {
    val result = IgnoringErrorHandler.handleError(new RuntimeException("Unexpected error occured!"), "url")
    result should be (Seq())
  }
}
