package com.illesguy.webcrawler.parsing

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.util.Try

trait JsoupDocumentRetriever {
  def getDocumentFromUrl(url: String): Try[Document]
}

object JsoupDocumentRetriever extends JsoupDocumentRetriever {
  override def getDocumentFromUrl(url: String): Try[Document] = Try(Jsoup.connect(url).get)
}
