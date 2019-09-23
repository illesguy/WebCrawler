package com.illesguy.webcrawler.parser

import org.jsoup.nodes.Document

trait UrlParser {
  def getUrlsFromDocument(document: Document): Seq[String]
}
