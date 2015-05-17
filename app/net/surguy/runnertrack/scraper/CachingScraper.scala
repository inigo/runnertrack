package net.surguy.runnertrack.scraper

import net.sf.ehcache.{CacheManager, Element}
import net.surguy.runnertrack.model.Runner
import org.openqa.selenium.WebDriver

/**
 * Cache runners - both to stop us from hitting the backend too quickly, and so if the backend
 * goes down then we can still show the latest data that we have.
 */
class CachingScraper(delegate: RaceScraper) extends RaceScraper {
  private val cacheName = delegate.getClass.getCanonicalName
  CacheManager.getInstance().addCache(cacheName)

  override def scrape(browser: WebDriver)(runnerId: String): Runner = delegate.scrape(browser)(runnerId)

  override def tryScrape(browser: WebDriver)(runnerId: String): Option[Runner] = {
    val cache = CacheManager.getInstance().getCache(cacheName)
    val cacheEntry = Option(cache.get(runnerId))
    val minRefreshAge = System.currentTimeMillis() - 10000
    cacheEntry match {
      // If there's a cache entry, and it's too soon since we last polled, then just return the cached runner
      case Some(entry) if entry.getLatestOfCreationAndUpdateTime > minRefreshAge =>
        Some( entry.getObjectValue.asInstanceOf[Runner] )
      case _ =>
        // Otherwise, call the underlying delegate to look up its value
        val newRunner = super.tryScrape(browser)(runnerId)
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
