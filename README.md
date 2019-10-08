# WebCrawler
WebCrawler built with Scala

To build run `gradle build` then run with `build/bin/WebCrawler [url_to_crawl]`.

Parameters:
 * url_to_crawl - The url to start crawling to sub domains from, if omitted it will default to https://www.google.com

The application has a 5 minute timeout on it after which it will terminate regardless of whether it has finished crawling
or not. It is not suitable for crawling through larger domains as it would timeout without changing the timeout value in
the configuration properties. Testing with https://monzo.com as the input, took around 30 seconds.

Enhancement options:
 * Input argument parser to pass in custom timeout, retry count etc.
 * Implement with Akka actors
 * Create custom site map creator class
