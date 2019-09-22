package com.illesguy.webcrawler.main

import com.illesguy.webcrawler.parsing.{JsoupDocumentRetriever, SubDomainUrlParser}

object Main extends App {
  val urlToCrawl = args.toSeq.headOption.getOrElse("https://monzo.com")

  val parser = new SubDomainUrlParser(JsoupDocumentRetriever)
  parser.getUrls(urlToCrawl).foreach(println)
}
