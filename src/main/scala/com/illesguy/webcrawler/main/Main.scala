package com.illesguy.webcrawler.main

import java.util.concurrent.Executors

import com.illesguy.webcrawler.crawler.Crawler
import com.illesguy.webcrawler.errorhandler.IgnoringErrorHandler
import com.illesguy.webcrawler.parser.SubDomainUrlParser
import com.illesguy.webcrawler.processor.JsoupPageProcessor
import org.slf4j.LoggerFactory

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

object Main extends App {
  val logger = LoggerFactory.getLogger(getClass.getCanonicalName)

  val urlToCrawl = args.toSeq.headOption.getOrElse("https://monzo.com")

  val errorHandler = IgnoringErrorHandler
  val parser = SubDomainUrlParser
  val retriesOnTimeout = 3
  val threadCount = 8
  val execCtx = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(threadCount))
  val pageProcessor = new JsoupPageProcessor(retriesOnTimeout, parser, errorHandler)

  val crawler = new Crawler(pageProcessor, execCtx)

  logger.info(s"Starting crawling from $urlToCrawl with $retriesOnTimeout retries on timeout on $threadCount threads")

  val startTime = System.currentTimeMillis()
  val urls = Await.result(crawler.crawlUrl(urlToCrawl), Duration.Inf)
  val endTime = System.currentTimeMillis()
  val executionTime = endTime - startTime

  urls.foreach(println)
  logger.info(s"Found ${urls.size} urls in $executionTime ms.")
}
