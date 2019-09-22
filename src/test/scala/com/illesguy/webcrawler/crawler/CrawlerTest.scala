package com.illesguy.webcrawler.crawler

import java.util.concurrent.Executors

import com.illesguy.webcrawler.errorhandler.ErrorHandler
import com.illesguy.webcrawler.parser.UrlParser
import org.jsoup.HttpStatusException
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Matchers.{eq => eqTo, _}
import org.scalatest.{FlatSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

@RunWith(classOf[JUnitRunner])
class CrawlerTest extends FlatSpec with Matchers with MockitoSugar {

  val mockParser = mock[UrlParser]
  val mockErrorHandler = mock[ErrorHandler]
  val urlRetrievalError = new HttpStatusException("Couldn't retrieve urls.", 404, "url")
  val retriesOnTimeout = 0
  val execCtx = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))


  val crawler = new Crawler(mockParser, mockErrorHandler, retriesOnTimeout, execCtx)

  def resetMocks(): Unit = {
    Mockito.reset(mockParser)
    Mockito.reset(mockErrorHandler)

    Mockito.when(mockErrorHandler.handleError(any(classOf[HttpStatusException]), anyString())).thenReturn(Seq())

    Mockito.when(mockParser.getUrls(anyString, anyInt)).thenReturn(Success(Seq()))
    Mockito.when(mockParser.getUrls(eqTo("start1"), anyInt)).thenReturn(Success(Seq("start1/sub1", "start1/sub2")))
    Mockito.when(mockParser.getUrls(eqTo("start1/sub1"), anyInt)).thenReturn(Success(Seq("start1/sub1/subsub1", "start1/sub1/subsub2")))
    Mockito.when(mockParser.getUrls(eqTo("start1/sub2"), anyInt)).thenReturn(Success(Seq("start1/sub2/subsub1", "start1/sub2/subsub2")))

    Mockito.when(mockParser.getUrls(eqTo("start2"), anyInt)).thenReturn(Success(Seq("start2/sub1", "start2/sub2")))
    Mockito.when(mockParser.getUrls(eqTo("start2/sub1"), anyInt)).thenReturn(Failure(urlRetrievalError))
    Mockito.when(mockParser.getUrls(eqTo("start2/sub2"), anyInt)).thenReturn(Success(Seq("start2/sub2/subsub1", "start2/sub2/subsub2")))
  }

  "Crawler" should "crawl to all url returned by url parser" in {
    resetMocks()

    val expectedResult = Seq("start1", "start1/sub1", "start1/sub1/subsub1", "start1/sub1/subsub2",
      "start1/sub2", "start1/sub2/subsub1", "start1/sub2/subsub2")
    val result = crawler.crawlUrl("start1")

    result should be (expectedResult)
    Mockito.verify(mockParser, Mockito.times(7)).getUrls(anyString, eqTo(retriesOnTimeout))
    Mockito.verify(mockErrorHandler, Mockito.never).handleError(any(classOf[HttpStatusException]), anyString())
  }

  it should "use UrlErrorHandler to handle any exceptions" in {
    resetMocks()

    val expectedResult = Seq("start2", "start2/sub2", "start2/sub2/subsub1", "start2/sub2/subsub2")
    val result = crawler.crawlUrl("start2")

    result should be (expectedResult)
    Mockito.verify(mockParser, Mockito.times(5)).getUrls(anyString, eqTo(retriesOnTimeout))
    Mockito.verify(mockErrorHandler, Mockito.times(1)).handleError(eqTo(urlRetrievalError), anyString)
  }
}
