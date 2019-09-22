package com.illesguy.webcrawler.parser

import scala.util.Try

trait UrlParser {
  def getUrls(url: String, retryCount: Int): Try[Seq[String]]
}
