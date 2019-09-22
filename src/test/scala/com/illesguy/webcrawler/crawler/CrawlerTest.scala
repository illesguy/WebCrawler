package com.illesguy.webcrawler.crawler

import java.util.concurrent.Executors

import com.illesguy.webcrawler.parser.UrlParser
import org.jsoup.HttpStatusException
import org.junit.runner.RunWith
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito
import org.scalatest.{FlatSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success, Try}

@RunWith(classOf[JUnitRunner])
class CrawlerTest extends FlatSpec with Matchers with MockitoSugar {

  val mockParser = mock[UrlParser]
  val urlRetrievalError = new HttpStatusException("Couldn't retrieve urls.", 404, "url")
  val retriesOnTimeout = 0
  val execCtx = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))


  val crawler = new Crawler(mockParser, retriesOnTimeout, execCtx)

  def resetMocks(): Unit = {
    Mockito.reset(mockParser)

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
    val result = Await.result(crawler.crawlUrl("start1"), Duration.Inf)

    result should be (expectedResult)
    Mockito.verify(mockParser, Mockito.times(7)).getUrls(anyString, eqTo(retriesOnTimeout))
  }

  it should "use return a failure at when the future ends if the url parser returned a failure" in {
    resetMocks()

    val result = Try(Await.result(crawler.crawlUrl("start2"), Duration.Inf))

    result should be (Failure(urlRetrievalError))
    Mockito.verify(mockParser, Mockito.times(5)).getUrls(anyString, eqTo(retriesOnTimeout))
  }
}
