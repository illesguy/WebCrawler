package com.illesguy.webcrawler.errorhandler

trait ErrorHandler {
  def handleError(ex: Throwable, url: String): Seq[String]
}
