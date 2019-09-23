package com.illesguy.webcrawler.errorhandler

import org.jsoup.{HttpStatusException, UnsupportedMimeTypeException}

object IgnoringErrorHandler extends ErrorHandler {
  override def handleError(ex: Throwable, url: String): Seq[String] = ex match {
    case _: HttpStatusException => Seq()
    case _: UnsupportedMimeTypeException => Seq()
    case e => throw e
  }
}
