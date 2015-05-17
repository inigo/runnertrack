package net.surguy.runnertrack.scraper

import net.sf.ehcache.{CacheManager, Element}
import net.surguy.runnertrack.model.Runner
import org.openqa.selenium.WebDriver

import scala.util.Try

/**
 * Cache runners - both to stop us from hitting the backend too quickly, and so if the backend
 * goes down then we can still show the latest data that we have.
 */
class CachingScraper(delegate: RaceScraper, minRefreshTimeInMs: Long = 10000, cacheNameSuffix: String = "") extends RaceScraper {
  // The tests sometimes fail if just using the class name as the key - because the mock delegates sometimes share the same name
  private val cacheName = delegate.getClass.getCanonicalName + cacheNameSuffix
  CacheManager.getInstance().addCacheIfAbsent(cacheName)

  override def scrape(browser: WebDriver)(runnerId: String): Runner = delegate.scrape(browser)(runnerId)

  override def tryScrape(browser: WebDriver)(runnerId: String): Option[Runner] = {
    val cache = CacheManager.getInstance().getCache(cacheName)
    val cacheEntry = Option(cache.get(runnerId))
    val minRefreshAge = System.currentTimeMillis() - minRefreshTimeInMs
    cacheEntry match {
      // If there's a cache entry, and it's too soon since we last polled, then just return the cached runner
      case Some(entry) if entry.getLatestOfCreationAndUpdateTime > minRefreshAge && minRefreshTimeInMs>0 =>
        Some( entry.getObjectValue.asInstanceOf[Runner] )
      case _ =>
        // Otherwise, call the underlying delegate to look up its value
        val newRunner = Try(delegate.scrape(browser)(runnerId)).toOption
        newRunner match {
          // If we're unable to retrieve a new runner, return the cached one if there is one (or None if not)
          case None => cacheEntry.map( _.getObjectValue.asInstanceOf[Runner] )
          // Otherwise, update the cache and return the new runner
          case Some(runner) =>
            cache.put(new Element(runnerId, runner))
            newRunner
        }
    }
  }

}
