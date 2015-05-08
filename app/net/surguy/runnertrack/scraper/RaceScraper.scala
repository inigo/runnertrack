package net.surguy.runnertrack.scraper

import java.time.format.DateTimeFormatter
import java.time.temporal.{TemporalAccessor, TemporalQuery}
import java.time.{Duration, LocalTime}

import com.gargoylesoftware.htmlunit.BrowserVersion
import net.surguy.runnertrack.model._
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.{By, SearchContext, WebDriver}

import scala.util.Try

abstract class RaceScraper {
  def scrape(runnerId: String): Runner
}

trait WebDriverTools {
  val browser: WebDriver
  def $(cssSelector: String, el: SearchContext = browser) = el.findElement(By.cssSelector(cssSelector)).getText.trim
  def $x(xpath: String, el: SearchContext = browser) = el.findElement(By.xpath(xpath)).getText.trim
}

object RaceScraper {
  def browser() = createHtmlUnitDriver(enableJavaScript = false)

  private def createHtmlUnitDriver(enableJavaScript: Boolean): WebDriver = {
    val driver = new HtmlUnitDriver(BrowserVersion.FIREFOX_24)
    driver.setJavascriptEnabled(enableJavaScript)
    driver
  }

  def tryParseTime(time: String): Option[LocalTime] = Try(if (time.length>2) Some(parseTime(time)) else None).toOption.flatten
  def parseTime(time: String) = DateTimeFormatter.ISO_LOCAL_TIME.parse(time, new TemporalQuery[LocalTime] {
    override def queryFrom(temporal: TemporalAccessor) = LocalTime.from(temporal)
  })

  def tryParseDuration(time: String): Option[Duration] = Try(if (time.length>2) Some(parseDuration(time)) else None).toOption.flatten
  def parseDuration(time: String): Duration = Duration.ofSeconds(parseTime(time).toSecondOfDay)

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
        Distance(distance.replaceAll("[^0-9]","").toDouble, Mile)
      case _ =>
        Distance(distance.replaceAll("[^0-9]","").toDouble, Km)
    }
  }
}
