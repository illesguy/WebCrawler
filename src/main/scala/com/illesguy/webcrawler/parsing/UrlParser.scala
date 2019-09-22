package com.illesguy.webcrawler.parsing

import scala.util.Try

trait UrlParser {
  def getUrls(url: String): Try[Seq[String]]
}
