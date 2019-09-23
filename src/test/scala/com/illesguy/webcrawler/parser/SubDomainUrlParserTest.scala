package com.illesguy.webcrawler.parser

import org.jsoup.Jsoup
import org.junit.runner.RunWith
import org.scalatest.{FlatSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner
import org.scalatestplus.mockito.MockitoSugar

@RunWith(classOf[JUnitRunner])
class SubDomainUrlParserTest extends FlatSpec with Matchers with MockitoSugar {

  val urlParser = SubDomainUrlParser

  "SubDomainUrlParser" should "return only sub domain urls" in {
    val url = "https://mock_page.com/"
    val htmlStream = getClass.getClassLoader.getResourceAsStream("html/mock_page.html")
    val document = Jsoup.parse(htmlStream, "UTF-8", url)

    val result = urlParser.getUrlsFromDocument(document)

    result should be (Seq(f"${url}about", f"${url}more_info"))
  }

  it should "not return itself among sub domain urls" in {
    val url = "https://self_referential.com"
    val htmlStream = getClass.getClassLoader.getResourceAsStream("html/self_referential.html")
    val document = Jsoup.parse(htmlStream, "UTF-8", url)

    val result = urlParser.getUrlsFromDocument(document)

    result should be (Seq(f"$url/about"))
  }

  it should "return duplicate urls only once" in {
    val url = "https://duplicates.com"
    val htmlStream = getClass.getClassLoader.getResourceAsStream("html/duplicates.html")
    val document = Jsoup.parse(htmlStream, "UTF-8", url)

    val result = urlParser.getUrlsFromDocument(document)

    result should be (Seq(f"$url/about", f"$url/stuff"))
  }

  it should "ignore anchors in urls" in {
    val url = "https://urls_with_anchors.com"
    val htmlStream = getClass.getClassLoader.getResourceAsStream("html/urls_with_anchors.html")
    val document = Jsoup.parse(htmlStream, "UTF-8", url)

    val result = urlParser.getUrlsFromDocument(document)

    result should be (Seq(f"$url/about", f"$url/stuff"))
  }

  it should "ignore anchors in duplicate urls" in {
    val url = "https://duplicates_with_anchors.com"
    val htmlStream = getClass.getClassLoader.getResourceAsStream("html/duplicates_with_anchors.html")
    val document = Jsoup.parse(htmlStream, "UTF-8", url)

    val result = urlParser.getUrlsFromDocument(document)

    result should be (Seq(f"$url/about", f"$url/stuff"))
  }

  it should "ignore special characters in urls" in {
    val url = "https://urls_with_special_characters.com"
    val htmlStream = getClass.getClassLoader.getResourceAsStream("html/urls_with_special_characters.html")
    val document = Jsoup.parse(htmlStream, "UTF-8", url)

    val result = urlParser.getUrlsFromDocument(document)

    result should be (Seq(f"$url/about", f"$url/stuff"))
  }
}
