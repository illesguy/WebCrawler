package com.illesguy.webcrawler.crawler

import com.illesguy.webcrawler.processor.PageProcessor

import scala.concurrent.{ExecutionContext, Future}

class Crawler(pageProcessor: PageProcessor, implicit val executionContext: ExecutionContext) {

  def crawlUrl(startUrl: String, currentDepth: Int = 0): Future[Seq[(Int, String)]] = Future(
    pageProcessor.getUrlsFromWebPage(startUrl)
  ).flatMap { urls =>
    Future.sequence(urls.map(u => crawlUrl(u, currentDepth + 1))).map(foundUrls => (currentDepth, startUrl) +: foundUrls.flatten)
  }
}
