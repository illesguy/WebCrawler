package com.illesguy.webcrawler.crawler

import com.illesguy.webcrawler.parser.UrlParser

import scala.concurrent.{ExecutionContext, Future}

class Crawler(urlParser: UrlParser, retriesOnTimeout: Int, implicit val executionContext: ExecutionContext) {

  def crawlUrl(startUrl: String): Future[Seq[String]] = Future.fromTry(
    urlParser.getUrls(startUrl, retriesOnTimeout)
  ).flatMap { urls =>
    Future.sequence(urls.map(crawlUrl)).map(startUrl +: _.flatten)
  }
}
