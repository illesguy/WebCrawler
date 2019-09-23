package com.illesguy.webcrawler.crawler

import com.illesguy.webcrawler.processor.PageProcessor

import scala.concurrent.{ExecutionContext, Future}

class Crawler(pageProcessor: PageProcessor, implicit val executionContext: ExecutionContext) {

  def crawlUrl(startUrl: String): Future[Seq[String]] = Future.fromTry(
    pageProcessor.getUrlsFromWebPage(startUrl)
  ).flatMap { urls =>
    Future.sequence(urls.map(crawlUrl)).map(startUrl +: _.flatten)
  }
}
