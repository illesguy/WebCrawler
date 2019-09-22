package com.illesguy.webcrawler.parsing

import scala.jdk.CollectionConverters._
import scala.util.Try


class SubDomainUrlParser(documentRetriever: JsoupDocumentRetriever) extends UrlParser {

  override def getUrls(url: String): Try[Seq[String]] = documentRetriever.getDocumentFromUrl(url).map { doc =>
    lazy val elements = doc.select("a[href]").asScala.toSeq
    lazy val urls = elements.map(e => getCanonicalUrl(e.attr("abs:href")))
    lazy val subDomainUrls = urls.filter(u => isSubDomain(url, u))
    subDomainUrls.distinct
  }

  private def getCanonicalUrl(url: String): String = {
    if (url.contains("#"))
      url.substring(0, url.indexOf('#'))
    else if (url.endsWith("/"))
      url.substring(0, url.length - 1)
    else url
  }

  private def isSubDomain(root: String, url: String): Boolean = url.startsWith(root) && url != root
}
