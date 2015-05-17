package net.surguy.runnertrack.scraper

import java.util.UUID

import net.surguy.runnertrack.model._
import org.openqa.selenium.WebDriver
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class CachingScraperSpec extends Specification with Mockito {
  isolated

  val runner = Runner("Test user", Seq(), "Test club", None, None)
  val anotherRunner = Runner("Test user 2", Seq(), "Test club 2", None, None)
  val delegate = mock[RaceScraper]
  def retrieve(runnerId: String)(implicit cache: CachingScraper) = cache.tryScrape(RaceScraper.browser())(runnerId)
  def uuid() = "_" + UUID.randomUUID().toString

  "Retrieving runners from the cache" should {
    implicit val cache = new CachingScraper(delegate, cacheNameSuffix = uuid())
    "retrieve a runner from the underlying store" in {
      delegate.scrape(any[WebDriver])(any[String]) returns runner
      retrieve("513432") mustEqual Some(runner)
      there was one(delegate).scrape(any[WebDriver])(any[String])
    }
    "return None if the runner doesn't exist in the underlying store" in {
      delegate.scrape(any[WebDriver])(any[String]) throws new IllegalArgumentException()
      retrieve("noSuchId") mustEqual None
      there was one(delegate).scrape(any[WebDriver])(any[String])
    }
  }

  "Making two calls to the cache" should {
    "if done in quick succession, only make one call to the underlying store" in {
      implicit val cache = new CachingScraper(delegate, 10000, cacheNameSuffix = uuid())

      delegate.scrape(any[WebDriver])(any[String]) returns runner
      retrieve("513432") mustEqual Some(runner)
      retrieve("513432") mustEqual Some(runner)
      there was one(delegate).scrape(any[WebDriver])(any[String])
    }
    "if done with a pause, make two calls to the underlying store" in {
      implicit val cache = new CachingScraper(delegate, 1, cacheNameSuffix = uuid())

      delegate.scrape(any[WebDriver])(any[String]) returns runner
      retrieve("513432") mustEqual Some(runner)
      Thread.sleep(10)
      retrieve("513432") mustEqual Some(runner)
      there were two(delegate).scrape(any[WebDriver])(any[String])
    }
  }

  "Calling the cache when there is an already cached value" should {
    "use a new value if there is one" in {
      implicit val cache = new CachingScraper(delegate, 0, cacheNameSuffix = uuid())
      delegate.scrape(any[WebDriver])(any[String]) returns runner thenReturns anotherRunner
      retrieve("513432") mustEqual Some(runner)
      retrieve("513432") mustEqual Some(anotherRunner)
    }
    "use the old value if the underlying store is now returning an error" in {
      implicit val cache = new CachingScraper(delegate, 0, cacheNameSuffix = uuid())
      delegate.scrape(any[WebDriver])(any[String]) returns runner thenThrows new IllegalArgumentException()
      retrieve("513432") mustEqual Some(runner)
      retrieve("513432") mustEqual Some(runner)
    }
  }

}
