package com.illesguy.webcrawler.crawler

import java.util.concurrent.Executors

import com.illesguy.webcrawler.processor.PageProcessor
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito
import org.scalatest.{FlatSpec, Matchers}
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Try}

class CrawlerTest extends FlatSpec with Matchers with MockitoSugar {

  val mockProcessor = mock[PageProcessor]
  val urlRetrievalError = new RuntimeException("Couldn't retrieve urls")
  val execCtx = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))


  val crawler = new Crawler(mockProcessor, execCtx)

  def resetMocks(): Unit = {
    Mockito.reset(mockProcessor)

    Mockito.when(mockProcessor.getUrlsFromWebPage(anyString)).thenReturn(Seq())
    Mockito.when(mockProcessor.getUrlsFromWebPage(eqTo("start1"))).thenReturn(Seq("start1/sub1", "start1/sub2"))
    Mockito.when(mockProcessor.getUrlsFromWebPage(eqTo("start1/sub1"))).thenReturn(Seq("start1/sub1/subsub1", "start1/sub1/subsub2"))
    Mockito.when(mockProcessor.getUrlsFromWebPage(eqTo("start1/sub2"))).thenReturn(Seq("start1/sub2/subsub1", "start1/sub2/subsub2"))
    Mockito.when(mockProcessor.getUrlsFromWebPage(eqTo("start2"))).thenReturn(Seq("start2/sub1", "start2/sub2"))
    Mockito.when(mockProcessor.getUrlsFromWebPage(eqTo("start2/sub1"))).thenThrow(urlRetrievalError)
    Mockito.when(mockProcessor.getUrlsFromWebPage(eqTo("start2/sub2"))).thenReturn(Seq("start2/sub2/subsub1", "start2/sub2/subsub2"))
  }

  "Crawler" should "crawl to all url returned by url parser" in {
    resetMocks()

    val expectedResult = Seq((0, "start1"), (1, "start1/sub1"), (2, "start1/sub1/subsub1"), (2, "start1/sub1/subsub2"),
      (1, "start1/sub2"), (2, "start1/sub2/subsub1"), (2, "start1/sub2/subsub2"))
    val result = Await.result(crawler.crawlUrl("start1"), Duration.Inf)

    result should be (expectedResult)
    Mockito.verify(mockProcessor, Mockito.times(7)).getUrlsFromWebPage(anyString)
  }

  it should "use return a failure at when the future ends if the url parser returned a failure" in {
    resetMocks()

    val result = Try(Await.result(crawler.crawlUrl("start2"), Duration.Inf))

    result should be (Failure(urlRetrievalError))
    Mockito.verify(mockProcessor, Mockito.times(5)).getUrlsFromWebPage(anyString)
  }
}
