package com.illesguy.webcrawler.parsing

trait UrlParser {
  def getUrls(url: String): Seq[String]
}
