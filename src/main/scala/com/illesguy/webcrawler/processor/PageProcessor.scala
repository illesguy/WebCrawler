package com.illesguy.webcrawler.processor

trait PageProcessor {

  def getUrlsFromWebPage(pageUrl: String): Seq[String]

}
