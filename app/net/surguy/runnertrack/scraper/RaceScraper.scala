package net.surguy.runnertrack.scraper

import java.util.concurrent.TimeUnit

import com.gargoylesoftware.htmlunit.BrowserVersion
import net.surguy.runnertrack.model._
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.{By, SearchContext, WebDriver}

import scala.util.Try

abstract class RaceScraper {
  private[scraper] def scrape(browser: WebDriver)(runnerId: String): Runner
  def tryScrape(browser: WebDriver)(runnerId: String): Option[Runner] = Try(scrape(browser)(runnerId)).toOption
  def cacheKey: String
}

trait WebDriverTools {
  def $(cssSelector: String, el: SearchContext = null)(implicit browser: WebDriver) = nonNullOf(el, browser).findElement(By.cssSelector(cssSelector)).getText.trim
  def $x(xpath: String, el: SearchContext = null)(implicit browser: WebDriver) = nonNullOf(el, browser).findElement(By.xpath(xpath)).getText.trim
  private def nonNullOf(a: SearchContext, b:SearchContext): SearchContext = if (a!=null) a else b
}

object RaceScraper {
  def browser() = createHtmlUnitDriver(enableJavaScript = false)

  private def createHtmlUnitDriver(enableJavaScript: Boolean): WebDriver = {
    val driver = new HtmlUnitDriver(BrowserVersion.FIREFOX_24)
    driver.setJavascriptEnabled(enableJavaScript)
    driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS)
    driver.manage().timeouts().setScriptTimeout(10, TimeUnit.SECONDS)
    driver
  }

}

abstract class DistanceParser {
  def parseDistance(distance: String): Distance
}

class GenericDistanceParser extends DistanceParser {
  override def parseDistance(distance: String) = {
    distance match {
      case s if s.toUpperCase.contains("HALF") => Distances.HalfMarathon
      case s if s.toUpperCase.contains("FINISH") => Distances.Marathon
      case s if s.toUpperCase.contains("MILE") =>
        Distance(distance.replaceAll("[^0-9.]","").toDouble, Mile)
      case _ =>
        Distance(distance.replaceAll("[^0-9.]","").toDouble, Km)
    }
  }
}

/** Convert a race number into the identifier needed for tracking that runner. */
trait RunnerFinder {
  def findRunnerId(browser:WebDriver)(raceNumber: String): Option[String]
  def cacheKey: String
}

class NoopRunnerFinder extends RunnerFinder {
  override def findRunnerId(browser: WebDriver)(raceNumber: String): Option[String] = None
  def cacheKey = "noop"
}