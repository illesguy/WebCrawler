package com.illesguy.webcrawler.errorhandler

import org.jsoup.{HttpStatusException, UnsupportedMimeTypeException}

object LoggingErrorHandler extends ErrorHandler {
  override def handleError(ex: Throwable, url: String): Seq[String] = ex match {
    case e: HttpStatusException => Seq(f"$url => ${e.getStatusCode}: ${e.getMessage}")

    case e: UnsupportedMimeTypeException => Seq(f"$url => ${e.getMimeType}: ${e.getMessage}")

    case e => throw e
  }
}
