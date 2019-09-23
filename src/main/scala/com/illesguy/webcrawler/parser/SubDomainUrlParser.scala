package com.illesguy.webcrawler.parser

import org.jsoup.nodes.Document

import scala.jdk.CollectionConverters._


object SubDomainUrlParser extends UrlParser {

  private val specialCharacters = raw"[#\\?%]".r

  override def getUrlsFromDocument(document: Document): Seq[String] = {
    lazy val elements = document.select("a[href]").asScala.toSeq
    lazy val urls = elements.map(e => getCanonicalUrl(e.attr("abs:href")))
    lazy val subDomainUrls = urls.filter(u => isSubDomain(document.baseUri, u))
    subDomainUrls.distinct
  }

  private def getCanonicalUrl(url: String): String = {
    val urlWithoutSpecialChars = specialCharacters.findFirstIn(url).map(c => url.substring(0, url.indexOf(c))).getOrElse(url)

    if (urlWithoutSpecialChars.endsWith("/"))
      urlWithoutSpecialChars.substring(0, urlWithoutSpecialChars.length - 1)
    else urlWithoutSpecialChars
  }

  private def isSubDomain(baseUrl: String, url: String): Boolean = url.startsWith(baseUrl) && url != baseUrl

}
