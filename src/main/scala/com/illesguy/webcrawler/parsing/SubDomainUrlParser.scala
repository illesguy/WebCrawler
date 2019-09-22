package com.illesguy.webcrawler.parsing

import scala.jdk.CollectionConverters._
import scala.util.Success


class SubDomainUrlParser(documentRetriever: JsoupDocumentRetriever) extends UrlParser {

  override def getUrls(url: String): Seq[String] = documentRetriever.getDocumentFromUrl(url) match {
    case Success(doc) => doc.select("a[href]").asScala.toSeq.map(_.attr("abs:href")).filter(isSubDomain(url, _))
    case _ => Seq()
  }

  private def isSubDomain(root: String, url: String): Boolean = url.startsWith(root) && url != root && url != f"$root/"
}
