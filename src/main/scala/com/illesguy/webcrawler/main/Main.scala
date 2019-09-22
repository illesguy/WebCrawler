package com.illesguy.webcrawler.main

import java.util.concurrent.Executors

import com.illesguy.webcrawler.crawler.Crawler
import com.illesguy.webcrawler.errorhandler.LoggingErrorHandler
import com.illesguy.webcrawler.parser.{JsoupDocumentRetriever, SubDomainUrlParser}

import scala.concurrent.ExecutionContext

object Main extends App {
  val urlToCrawl = args.toSeq.headOption.getOrElse("https://monzo.com")

  val parser = new SubDomainUrlParser(JsoupDocumentRetriever)
  val errorHandler = LoggingErrorHandler
  val retriesOnTimeout = 3
  val threadCount = 5
  val execCtx = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(threadCount))

  val crawler = new Crawler(parser, errorHandler, retriesOnTimeout, execCtx)

  val urls = crawler.crawlUrl(urlToCrawl)

  println(urls.size)
  urls.foreach(println)
}
