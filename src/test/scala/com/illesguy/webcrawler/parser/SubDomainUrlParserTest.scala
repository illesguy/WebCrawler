package com.illesguy.webcrawler.parser

import java.net.SocketTimeoutException

import com.illesguy.webcrawler.errorhandler.ErrorHandler
import org.jsoup.{HttpStatusException, Jsoup}
import org.junit.runner.RunWith
import org.mockito.Matchers._
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.scalatest.{FlatSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner
import org.scalatestplus.mockito.MockitoSugar

import scala.util.{Failure, Success}

@RunWith(classOf[JUnitRunner])
class SubDomainUrlParserTest extends FlatSpec with Matchers with MockitoSugar {

  val mockDocumentRetriever = mock[JsoupDocumentRetriever]
  val mockErrorHandler = mock[ErrorHandler]
  val urlParser = new SubDomainUrlParser(mockDocumentRetriever, mockErrorHandler)

  def resetMocks(): Unit = {
    Mockito.reset(mockDocumentRetriever)
    Mockito.reset(mockErrorHandler)

    Mockito.when(mockDocumentRetriever.getDocumentFromUrl(anyString)).thenAnswer((invocation: InvocationOnMock) => {
      val url = invocation.getArguments.head.toString
      val htmlFileName = url.replace("https://", "html/").replace(".com", ".html")
      val htmlStream = getClass.getClassLoader.getResourceAsStream(htmlFileName)
      Success(Jsoup.parse(htmlStream, "UTF-8", url))
    })

    Mockito.when(mockErrorHandler.handleError(any(classOf[Throwable]), anyString)).thenAnswer {invocation: InvocationOnMock =>
      throw invocation.getArguments.head.asInstanceOf[Throwable]
    }
  }

  "SubDomainUrlParser" should "return only sub domain urls" in {
    resetMocks()
    val url = "https://mock_page.com/"

    val result = urlParser.getUrls(url, 0)

    result should be (Success(Seq(f"${url}about", f"${url}more_info")))
    Mockito.verify(mockErrorHandler, Mockito.never).handleError(any(classOf[Throwable]), anyString)
  }

  it should "not return itself among sub domain urls" in {
    resetMocks()
    val url = "https://self_referential.com"

    val result = urlParser.getUrls(url, 0)

    result should be (Success(Seq(f"$url/about")))
    Mockito.verify(mockErrorHandler, Mockito.never).handleError(any(classOf[Throwable]), anyString)
  }

  it should "return duplicate urls only once" in {
    resetMocks()
    val url = "https://duplicates.com"

    val result = urlParser.getUrls(url, 0)
    result should be (Success(Seq(f"$url/about", f"$url/stuff")))
    Mockito.verify(mockErrorHandler, Mockito.never).handleError(any(classOf[Throwable]), anyString)
  }

  it should "ignore anchors in urls" in {
    resetMocks()
    val url = "https://urls_with_anchors.com"

    val result = urlParser.getUrls(url, 0)

    result should be (Success(Seq(f"$url/about", f"$url/stuff")))
    Mockito.verify(mockErrorHandler, Mockito.never).handleError(any(classOf[Throwable]), anyString)
  }

  it should "retry socket timeouts as many times as specified and then pass upwards on call stack" in {
    Mockito.reset(mockDocumentRetriever)
    val urlSocketTimeoutError = new SocketTimeoutException("Call to URL timed out")
    Mockito.when(mockDocumentRetriever.getDocumentFromUrl("error")).thenReturn(Failure(urlSocketTimeoutError))

    val result = urlParser.getUrls("error", 3)

    result should be (Failure(urlSocketTimeoutError))
    Mockito.verify(mockDocumentRetriever, Mockito.times(4)).getDocumentFromUrl("error")
    Mockito.verify(mockErrorHandler, Mockito.times(4)).handleError(urlSocketTimeoutError, "error")
  }

  it should "use the error handler to try and handle non timeout errors" in {
    Mockito.reset(mockDocumentRetriever)
    val urlNotFoundError = new HttpStatusException("Page not found", 404, "error")
    Mockito.when(mockDocumentRetriever.getDocumentFromUrl("error")).thenReturn(Failure(urlNotFoundError))

    Mockito.reset(mockErrorHandler)
    Mockito.when(mockErrorHandler.handleError(any(classOf[HttpStatusException]), anyString)).thenReturn(Seq("good"))

    val result = urlParser.getUrls("error", 3)

    result should be (Success(Seq("good")))
    Mockito.verify(mockDocumentRetriever, Mockito.times(1)).getDocumentFromUrl("error")
    Mockito.verify(mockErrorHandler, Mockito.times(1)).handleError(urlNotFoundError, "error")
  }

  it should "return failure with the occurred error in it if it couldn't handle it with the error handler" in {
    resetMocks()
    val runtimeError = new RuntimeException("something unexpected happened")
    Mockito.when(mockDocumentRetriever.getDocumentFromUrl("error")).thenReturn(Failure(runtimeError))

    val result = urlParser.getUrls("error", 3)

    result should be (Failure(runtimeError))
    Mockito.verify(mockDocumentRetriever, Mockito.times(1)).getDocumentFromUrl("error")
    Mockito.verify(mockErrorHandler, Mockito.times(1)).handleError(runtimeError, "error")
  }
}
