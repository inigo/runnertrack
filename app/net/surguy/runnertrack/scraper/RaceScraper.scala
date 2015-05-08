package net.surguy.runnertrack.scraper

import java.time.{Duration, LocalTime, LocalDateTime}
import java.time.format.DateTimeFormatter
import java.time.temporal.{TemporalAccessor, TemporalQuery}

import com.gargoylesoftware.htmlunit.BrowserVersion
import net.surguy.runnertrack.model._
import org.openqa.selenium.{WebElement, By, WebDriver}
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import scala.collection.JavaConversions._
import scala.util.Try

abstract class RaceScraper {
  def scrape(runnerId: String): Runner
}

object RaceScraper {
  def browser() = createHtmlUnitDriver(enableJavaScript = false)

  private def createHtmlUnitDriver(enableJavaScript: Boolean): WebDriver = {
    val driver = new HtmlUnitDriver(BrowserVersion.FIREFOX_24)
    driver.setJavascriptEnabled(enableJavaScript)
    driver
  }

}

class LondonMarathon2015Scraper(browser: WebDriver) extends RaceScraper {

  // Paula Radcliffe: 9999990F5ECC830000171324
  val baseUrl = "http://results-2015.virginmoneylondonmarathon.com/2015/?content=detail&fpid=search&pid=search&idp=%s"

  private def $(cssSelector: String) = browser.findElement(By.cssSelector(cssSelector)).getText.trim
  private def $(cssSelector: String, el: WebElement) = el.findElement(By.cssSelector(cssSelector)).getText.trim

  override def scrape(runnerId: String) = {
    browser.navigate().to(baseUrl.format(runnerId))
    parse
  }

  def parse: Runner = {
    val name = $("td.f-__fullname")
    val club = $("td.f-club")
    val startTime = tryParseTime($("td.f-starttime_net"))

    val placeText = $("td.f-place_nosex")
    val place = if (placeText.matches("\\d+")) placeText.toInt else -1
    val finishTime = tryParseDuration($("td.f-time_finish_netto"))
    val finish = finishTime.map( t => Finish(place, t))

    val splits = for (row <- browser.findElements(By.cssSelector(".box-splits tr.split"))) yield {
      val distanceText = $("td.desc", row)
      val timeText = $("td.time", row)
      val duration = tryParseDuration(timeText)
      duration.map( d => Split(parseDistance(distanceText), d) )
    }

    Runner(name, splits.flatten, club, startTime, finish)
  }

  private def tryParseTime(time: String): Option[LocalTime] = Try(if (time.length>2) Some(parseTime(time)) else None).toOption.flatten
  private def parseTime(time: String) = DateTimeFormatter.ISO_LOCAL_TIME.parse(time, new TemporalQuery[LocalTime] {
    override def queryFrom(temporal: TemporalAccessor) = LocalTime.from(temporal)
  })

  private def tryParseDuration(time: String): Option[Duration] = Try(if (time.length>2) Some(parseDuration(time)) else None).toOption.flatten
  private def parseDuration(time: String): Duration = Duration.ofSeconds(parseTime(time).toSecondOfDay)

  private def parseDistance(distance: String): Distance = {
    distance match {
      case "HALF" => Distances.HalfMarathon
      case "FINISH" => Distances.Marathon
      case _ => Distance(distance.replaceAll("K","").toDouble, Km)
    }
  }

}