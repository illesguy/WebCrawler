package com.illesguy.webcrawler.processor

import java.net.SocketTimeoutException

import com.illesguy.webcrawler.errorhandler.ErrorHandler
import com.illesguy.webcrawler.parser.UrlParser
import org.jsoup.nodes.Document
import org.jsoup.{HttpStatusException, Jsoup}
import org.mockito.Matchers.{any, anyString}
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.scalatest.{FlatSpec, Matchers}
import org.scalatestplus.mockito.MockitoSugar

import scala.util.{Failure, Success}

class JsoupPageProcessorTest extends FlatSpec with Matchers with MockitoSugar {

  val mockUrlParser = mock[UrlParser]
  val mockErrorHandler = mock[ErrorHandler]
  val mockDocumentRetriever = mock[JsoupDocumentRetriever]
  val pageProcessor = new JsoupPageProcessor(3, mockUrlParser, mockErrorHandler, mockDocumentRetriever)

  def resetMocks(): Unit = {
    Mockito.reset(mockUrlParser)
    Mockito.reset(mockErrorHandler)
    Mockito.reset(mockDocumentRetriever)
  }

  "JsoupPageProcessor" should "retry socket timeouts as many times as specified and then pass upwards on call stack" in {
    resetMocks()
    val urlSocketTimeoutError = new SocketTimeoutException("Call to URL timed out")
    Mockito.when(mockDocumentRetriever.getDocumentFromUrl("error")).thenReturn(Failure(urlSocketTimeoutError))

    val result = pageProcessor.getUrlsFromWebPage("error")

    result should be (Failure(urlSocketTimeoutError))
    Mockito.verify(mockUrlParser, Mockito.never).getUrlsFromDocument(any(classOf[Document]))
    Mockito.verify(mockDocumentRetriever, Mockito.times(4)).getDocumentFromUrl("error")
    Mockito.verify(mockErrorHandler, Mockito.times(4)).handleError(urlSocketTimeoutError, "error")
  }

  it should "use the error handler to try and handle non timeout errors" in {
    resetMocks()
    val urlNotFoundError = new HttpStatusException("Page not found", 404, "error")
    Mockito.when(mockDocumentRetriever.getDocumentFromUrl("error")).thenReturn(Failure(urlNotFoundError))
    Mockito.when(mockErrorHandler.handleError(any(classOf[HttpStatusException]), anyString)).thenReturn(Seq("good"))

    val result = pageProcessor.getUrlsFromWebPage("error")

    result should be (Success(Seq("good")))
    Mockito.verify(mockUrlParser, Mockito.never).getUrlsFromDocument(any(classOf[Document]))
    Mockito.verify(mockDocumentRetriever, Mockito.times(1)).getDocumentFromUrl("error")
    Mockito.verify(mockErrorHandler, Mockito.times(1)).handleError(urlNotFoundError, "error")
  }

  it should "return failure with the occurred error in it if it couldn't handle it with the error handler" in {
    val runtimeError = new RuntimeException("something unexpected happened")
    Mockito.when(mockDocumentRetriever.getDocumentFromUrl("error")).thenReturn(Failure(runtimeError))
    Mockito.when(mockErrorHandler.handleError(any(classOf[Throwable]), anyString)).thenAnswer {invocation: InvocationOnMock =>
      throw invocation.getArguments.head.asInstanceOf[Throwable]
    }

    val result = pageProcessor.getUrlsFromWebPage("error")

    result should be (Failure(runtimeError))
    Mockito.verify(mockUrlParser, Mockito.never).getUrlsFromDocument(any(classOf[Document]))
    Mockito.verify(mockDocumentRetriever, Mockito.times(1)).getDocumentFromUrl("error")
    Mockito.verify(mockErrorHandler, Mockito.times(1)).handleError(runtimeError, "error")
  }

  it should "parse document with the url parser and return it in case of a success" in {
    resetMocks()
    val document = new Document("good")
    Mockito.when(mockDocumentRetriever.getDocumentFromUrl("good")).thenReturn(Success(document))
    Mockito.when(mockUrlParser.getUrlsFromDocument(any[Document])).thenReturn(Seq("good"))

    val result = pageProcessor.getUrlsFromWebPage("good")

    result should be (Success(Seq("good")))
    Mockito.verify(mockUrlParser, Mockito.times(1)).getUrlsFromDocument(document)
    Mockito.verify(mockDocumentRetriever, Mockito.never).getDocumentFromUrl(anyString)
    Mockito.verify(mockErrorHandler, Mockito.never).handleError(any(classOf[Throwable]), anyString)
  }
}
