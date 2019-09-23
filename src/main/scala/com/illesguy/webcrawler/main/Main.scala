package com.illesguy.webcrawler.main

import java.util.concurrent.Executors

import com.illesguy.webcrawler.crawler.Crawler
import com.illesguy.webcrawler.errorhandler.IgnoringErrorHandler
import com.illesguy.webcrawler.parser.{JsoupDocumentRetriever, SubDomainUrlParser}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Main extends App {
  val logger = LoggerFactory.getLogger(getClass.getCanonicalName)

  val urlToCrawl = args.toSeq.headOption.getOrElse("https://monzo.com")

  val errorHandler = IgnoringErrorHandler
  val parser = new SubDomainUrlParser(JsoupDocumentRetriever, errorHandler)
  val retriesOnTimeout = 3
  val threadCount = 6
  val execCtx = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(threadCount))

  val crawler = new Crawler(parser, retriesOnTimeout, execCtx)

  logger.info(s"Starting crawling from $urlToCrawl with $retriesOnTimeout retries on timeout on $threadCount threads")

  val startTime = System.currentTimeMillis()

  crawler.crawlUrl(urlToCrawl).onComplete {
    case Success(urls) =>
      val endTime = System.currentTimeMillis()

      urls.foreach(println)

      val executionTime = endTime - startTime
      logger.info(s"Found ${urls.size} urls in $executionTime ms.")

    case Failure(ex) =>
      logger.error("Exception occurred while crawling through the pages ", ex)

  }(execCtx)
}
