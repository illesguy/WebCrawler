package com.illesguy.webcrawler.main

import java.util.concurrent.ForkJoinPool

import com.illesguy.webcrawler.crawler.Crawler
import com.illesguy.webcrawler.errorhandler.IgnoringErrorHandler
import com.illesguy.webcrawler.parser.SubDomainUrlParser
import com.illesguy.webcrawler.processor.JsoupPageProcessor
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success, Try}

object Main extends App {
  val logger = LoggerFactory.getLogger(getClass.getCanonicalName)

  val urlToCrawl = args.toSeq.headOption.getOrElse("https://monzo.com")

  val errorHandler = IgnoringErrorHandler
  val parser = SubDomainUrlParser
  val retriesOnTimeout = 3
  val parallelism = 8
  val execCtx = ExecutionContext.fromExecutor(new ForkJoinPool(parallelism))
  val pageProcessor = new JsoupPageProcessor(retriesOnTimeout, parser, errorHandler)

  val crawler = new Crawler(pageProcessor, execCtx)

  logger.info(s"Starting crawling from $urlToCrawl")

  val threadsBefore = Thread.getAllStackTraces.keySet

  val startTime = System.currentTimeMillis()
  val crawlResult = Try(Await.result(crawler.crawlUrl(urlToCrawl), 5.minutes))
  val endTime = System.currentTimeMillis()
  val executionTime = endTime - startTime

  crawlResult match {
    case Success(urls) =>
      val siteMap = urls.map {
        case (depth, url) => f"|${"-".repeat(depth)} $url"
      }.mkString("\n")

      logger.info(s"Site crawling done, site map:\n$siteMap")
      logger.info(s"Found ${urls.size} urls in $executionTime ms.")

    case Failure(ex) =>
      logger.error("Error occurred while crawling through the websites!", ex)
  }
}
