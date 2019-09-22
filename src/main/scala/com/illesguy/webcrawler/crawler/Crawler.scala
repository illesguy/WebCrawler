package com.illesguy.webcrawler.crawler

import com.illesguy.webcrawler.errorhandler.ErrorHandler
import com.illesguy.webcrawler.parser.UrlParser

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class Crawler(urlParser: UrlParser, errorHandler: ErrorHandler, retriesOnTimeout: Int, implicit val executionContext: ExecutionContext) {

  def crawlUrl(startUrl: String): Seq[String] = urlParser.getUrls(startUrl, retriesOnTimeout) match {
    case Success(urls) => startUrl +: urls.flatMap(u => crawlUrl(u))
    case Failure(ex) => errorHandler.handleError(ex, startUrl)
  }
}
