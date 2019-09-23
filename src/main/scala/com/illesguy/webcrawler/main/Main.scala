package com.illesguy.webcrawler.main

import java.util.Properties
import java.util.concurrent.ForkJoinPool

import com.illesguy.webcrawler.crawler.Crawler
import com.illesguy.webcrawler.errorhandler.IgnoringErrorHandler
import com.illesguy.webcrawler.parser.SubDomainUrlParser
import com.illesguy.webcrawler.processor.JsoupPageProcessor
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

object Main extends App {
  val urlToCrawl = args.toSeq.headOption.getOrElse("https://monzo.com")
  val logger = LoggerFactory.getLogger(getClass.getCanonicalName)

  val properties: mutable.Map[String, String] = {
    val props = new Properties
    val propsStream = getClass.getClassLoader.getResourceAsStream("web-crawler.properties")
    props.load(propsStream)
    props.asScala
  }

  val retriesOnTimeout = properties.get("retriesOnTimeout").map(Integer.parseInt).getOrElse(3)
  val parallelism = properties.get("parallelism").map(Integer.parseInt).getOrElse(8)
  val crawlTimeoutSeconds = properties.get("crawlTimeoutSeconds").map(Integer.parseInt).getOrElse(120)
  val errorHandler = IgnoringErrorHandler
  val parser = SubDomainUrlParser
  val execCtx = ExecutionContext.fromExecutor(new ForkJoinPool(parallelism))
  val pageProcessor = new JsoupPageProcessor(retriesOnTimeout, parser, errorHandler)

  val crawler = new Crawler(pageProcessor, execCtx)

  logger.info(s"Starting crawling from $urlToCrawl")

  val startTime = System.currentTimeMillis()
  val crawlResult = Try(Await.result(crawler.crawlUrl(urlToCrawl), crawlTimeoutSeconds.seconds))
  val executionTime = System.currentTimeMillis() - startTime

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
