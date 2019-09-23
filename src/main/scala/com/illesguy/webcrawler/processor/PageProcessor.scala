package com.illesguy.webcrawler.processor

import scala.util.Try

trait PageProcessor {

  def getUrlsFromWebPage(pageUrl: String): Try[Seq[String]]

}
