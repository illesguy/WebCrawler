package com.illesguy.webcrawler.parser

import java.net.SocketTimeoutException

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
  val urlParser = new SubDomainUrlParser(mockDocumentRetriever)

  def resetMocks(): Unit = {
    Mockito.reset(mockDocumentRetriever)
    Mockito.when(mockDocumentRetriever.getDocumentFromUrl(anyString)).thenAnswer((invocation: InvocationOnMock) => {
      val url = invocation.getArguments.head.toString
      val htmlFileName = url.replace("https://", "html/").replace(".com", ".html")
      val htmlStream = getClass.getClassLoader.getResourceAsStream(htmlFileName)
      Success(Jsoup.parse(htmlStream, "UTF-8", url))
    })
  }

  "SubDomainUrlParser" should "return only sub domain urls" in {
    resetMocks()
    val url = "https://mock_page.com/"

    val result = urlParser.getUrls(url, 0)
    result should be (Success(Seq(f"${url}about", f"${url}more_info")))
  }

  it should "not return itself among sub domain urls" in {
    resetMocks()
    val url = "https://self_referential.com"

    val result = urlParser.getUrls(url, 0)
    result should be (Success(Seq(f"$url/about")))
  }

  it should "return duplicate urls only once" in {
    resetMocks()
    val url = "https://duplicates.com"

    val result = urlParser.getUrls(url, 0)
    result should be (Success(Seq(f"$url/about", f"$url/stuff")))
  }

  it should "ignore anchors in urls" in {
    resetMocks()
    val url = "https://urls_with_anchors.com"

    val result = urlParser.getUrls(url, 0)
    result should be (Success(Seq(f"$url/about", f"$url/stuff")))
  }

  it should "pass non timeout errors upwards the call stack" in {
    Mockito.reset(mockDocumentRetriever)
    val urlNotFoundError = new HttpStatusException("Page not found", 404, "error")
    Mockito.when(mockDocumentRetriever.getDocumentFromUrl("error")).thenReturn(Failure(urlNotFoundError))

    val result = urlParser.getUrls("error", 3)

    result should be (Failure(urlNotFoundError))
    Mockito.verify(mockDocumentRetriever, Mockito.times(1)).getDocumentFromUrl("error")
  }

  it should "retry socket timeouts as many times as specified and then pass upwards on call stack" in {
    Mockito.reset(mockDocumentRetriever)
    val urlSocketTimeoutError = new SocketTimeoutException("Call to URL timed out")
    Mockito.when(mockDocumentRetriever.getDocumentFromUrl("error")).thenReturn(Failure(urlSocketTimeoutError))

    val result = urlParser.getUrls("error", 3)

    result should be (Failure(urlSocketTimeoutError))
    Mockito.verify(mockDocumentRetriever, Mockito.times(4)).getDocumentFromUrl("error")
  }
}
