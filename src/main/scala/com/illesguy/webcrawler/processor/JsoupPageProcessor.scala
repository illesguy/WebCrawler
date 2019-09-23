package com.illesguy.webcrawler.processor
import java.net.SocketTimeoutException

import com.illesguy.webcrawler.errorhandler.ErrorHandler
import com.illesguy.webcrawler.parser.UrlParser
import org.slf4j.LoggerFactory

import scala.util.Try

class JsoupPageProcessor(retriesOnFailure: Int, urlParser: UrlParser, errorHandler: ErrorHandler, documentRetriever: JsoupDocumentRetriever = JsoupDocumentRetriever) extends PageProcessor {

  private val logger = LoggerFactory.getLogger(getClass.getCanonicalName)

  override def getUrlsFromWebPage(pageUrl: String): Try[Seq[String]] = getUrlsWithRetry(pageUrl, retriesOnFailure)

  private def getUrlsWithRetry(url: String, retryCount: Int): Try[Seq[String]] = documentRetriever.getDocumentFromUrl(url).map { doc =>
    logger.debug(f"Parsing page for $url")
    urlParser.getUrlsFromDocument(doc)
  }.recover {
    case ex =>
      logger.warn(s"Error occurred, while processing $url, invoking error handler")
      errorHandler.handleError(ex, url)
  }.recoverWith {
    case _: SocketTimeoutException if retryCount != 0 =>
      logger.warn(f"Call to $url timed out, retrying")
      getUrlsWithRetry(url, retryCount - 1)
  }
}
