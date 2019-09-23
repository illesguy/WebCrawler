package com.illesguy.webcrawler.parser

import java.net.SocketTimeoutException

import com.illesguy.webcrawler.errorhandler.ErrorHandler
import org.slf4j.LoggerFactory

import scala.jdk.CollectionConverters._
import scala.util.Try


class SubDomainUrlParser(documentRetriever: JsoupDocumentRetriever, errorHandler: ErrorHandler) extends UrlParser {

  private val logger = LoggerFactory.getLogger(getClass.getCanonicalName)

  override def getUrls(url: String, retryCount: Int): Try[Seq[String]] = documentRetriever.getDocumentFromUrl(url).map { doc =>
    logger.debug(f"Processed $url")
    lazy val elements = doc.select("a[href]").asScala.toSeq
    lazy val urls = elements.map(e => getCanonicalUrl(e.attr("abs:href")))
    lazy val subDomainUrls = urls.filter(u => isSubDomain(url, u))
    subDomainUrls.distinct
  }.recover {
    case ex =>
      logger.warn(s"Error occurred, while processing $url, invoking error handler")
      errorHandler.handleError(ex, url)
  }.recoverWith {
    case _: SocketTimeoutException if retryCount != 0 =>
      logger.warn(f"Call to $url timed out, retrying")
      getUrls(url, retryCount - 1)
  }

  private def getCanonicalUrl(url: String): String = {
    if (url.contains("#"))
      url.substring(0, url.indexOf('#'))
    else if (url.endsWith("/"))
      url.substring(0, url.length - 1)
    else url
  }

  private def isSubDomain(root: String, url: String): Boolean = url.startsWith(root) && url != root
}
