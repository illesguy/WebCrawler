package com.illesguy.webcrawler.processor
import java.net.SocketTimeoutException

import com.illesguy.webcrawler.errorhandler.ErrorHandler
import com.illesguy.webcrawler.parser.UrlParser
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

class JsoupPageProcessor(retriesOnFailure: Int, urlParser: UrlParser, errorHandler: ErrorHandler, documentRetriever: JsoupDocumentRetriever = JsoupDocumentRetriever) extends PageProcessor {

  private val logger = LoggerFactory.getLogger(getClass.getCanonicalName)

  override def getUrlsFromWebPage(pageUrl: String): Seq[String] = getUrlsWithRetry(pageUrl, retriesOnFailure)

  @scala.annotation.tailrec
  private def getUrlsWithRetry(url: String, retryCount: Int): Seq[String] = documentRetriever.getDocumentFromUrl(url) match {
    case Success(doc) =>
      logger.debug(f"Parsing page for $url")
      urlParser.getUrlsFromDocument(doc)
    case Failure(_: SocketTimeoutException) =>
      logger.warn(f"Call to $url timed out, retrying")
      getUrlsWithRetry(url, retryCount - 1)
    case Failure(ex) =>
      logger.warn(s"Error occurred, while processing $url, invoking error handler")
      errorHandler.handleError(ex, url)
  }
}
