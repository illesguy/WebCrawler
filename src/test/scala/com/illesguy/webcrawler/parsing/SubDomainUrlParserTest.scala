package com.illesguy.webcrawler.parsing

import org.jsoup.Jsoup
import org.junit.runner.RunWith
import org.mockito.Matchers._
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.scalatest.{FlatSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner
import org.scalatestplus.mockito.MockitoSugar

import scala.util.Success

@RunWith(classOf[JUnitRunner])
class SubDomainUrlParserTest extends FlatSpec with Matchers with MockitoSugar {

  val mockDocumentRetriever = mock[JsoupDocumentRetriever]
  Mockito.when(mockDocumentRetriever.getDocumentFromUrl(anyString)).thenAnswer((invocation: InvocationOnMock) => {
    val url = invocation.getArguments.head.toString
    val htmlFileName = url.replace("https://", "html/").replace(".com", ".html")
    val htmlStream = getClass.getClassLoader.getResourceAsStream(htmlFileName)
    Success(Jsoup.parse(htmlStream, "UTF-8", url))
  })
  val urlParser = new SubDomainUrlParser(mockDocumentRetriever)


  "SubDomainUrlParser" should "return only sub domain urls" in {
    val url = "https://mock_page.com/"
    val result = urlParser.getUrls(url)
    result should be (Seq(f"${url}about", f"${url}more_info"))
  }

  it should "not return itself among sub domain urls" in {
    val url = "https://self_referential.com"
    val result = urlParser.getUrls(url)
    result should be (Seq(f"$url/about"))
  }
}
