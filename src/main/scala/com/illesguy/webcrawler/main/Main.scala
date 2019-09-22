package com.illesguy.webcrawler.main

import java.util.concurrent.Executors

import com.illesguy.webcrawler.crawler.Crawler
import com.illesguy.webcrawler.errorhandler.IgnoringErrorHandler
import com.illesguy.webcrawler.parser.{JsoupDocumentRetriever, SubDomainUrlParser}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

object Main extends App {
  val urlToCrawl = args.toSeq.headOption.getOrElse("https://monzo.com")

  val errorHandler = IgnoringErrorHandler
  val parser = new SubDomainUrlParser(JsoupDocumentRetriever, errorHandler)
  val retriesOnTimeout = 3
  val threadCount = 4
  val execCtx = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(threadCount))

  val crawler = new Crawler(parser, retriesOnTimeout, execCtx)

  val startTime = System.currentTimeMillis()
  val urls = Await.result(crawler.crawlUrl(urlToCrawl), Duration.Inf)
  val endTime = System.currentTimeMillis()

  urls.foreach(println)

  val executionTime = endTime - startTime
  println(s"Found ${urls.size} urls in $executionTime ms.")
}
