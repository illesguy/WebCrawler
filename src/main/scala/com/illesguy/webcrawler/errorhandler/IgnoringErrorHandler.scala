package com.illesguy.webcrawler.errorhandler

object IgnoringErrorHandler extends ErrorHandler {
  override def handleError(ex: Throwable, url: String): Seq[String] = Seq()
}
