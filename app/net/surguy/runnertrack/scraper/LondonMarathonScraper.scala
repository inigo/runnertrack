package net.surguy.runnertrack.scraper

import net.surguy.runnertrack.model._
import org.openqa.selenium.{By, WebDriver}
import scala.collection.JavaConversions._
import net.surguy.runnertrack.TimeUtils._

class LondonMarathonScraper(year: String) extends RaceScraper with WebDriverTools {
  val distanceParser = new GenericDistanceParser()

  // Paula Radcliffe: 9999990F5ECC830000171324
  val baseUrl = s"http://results-$year.virginmoneylondonmarathon.com/$year/?content=detail&fpid=search&pid=search&idp=%s"

  override def scrape(browser: WebDriver)(runnerId: String) = {
    browser.navigate().to(baseUrl.format(runnerId))
    parse(browser)
  }

  override def cacheKey: String = "london"+year

  def parse(implicit browser: WebDriver): Runner = {
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
      duration.map( d => Split(distanceParser.parseDistance(distanceText), d) )
    }

    Runner(name, splits.flatten, club, startTime, finish)
  }

}

class LondonMarathonRunnerFinder(year: String) extends RunnerFinder with WebDriverTools {
  val baseUrl = s"http://results-$year.virginmoneylondonmarathon.com/$year/"

  override def findRunnerId(browser:WebDriver)(raceNumber: String): Option[String] = {
    openSearchResults(browser, raceNumber)
    parse(browser)
  }

  private[scraper] def parse(browser: WebDriver): Option[String] = {
    val link = browser.findElements(By.cssSelector(".list-table a")).headOption
    link.map(_.getAttribute("href")).map(href => extractId(href) )
  }

  private def openSearchResults(browser: WebDriver, raceNumber: String): Unit = {
    browser.navigate().to(baseUrl)
    val raceNumberField = browser.findElement(By.id("search-start_no"))
    raceNumberField.sendKeys(raceNumber)
    raceNumberField.submit()
  }

  private[scraper] def extractId(href: String): String = {
    // We want the "idp" part of the query string
    // http://results-2015.virginmoneylondonmarathon.com/2015/?content=detail&fpid=search&pid=search&idp=9999990F5ECC830000171324&lang=EN_CAP&event=MAS&search%5Bstart_no%5D=25371&search%5Bsex%5D=%25&search%5Bnation%5D=%25&search_sort=name&search_event=MAS
    val i = href.indexOf("idp=")
    href.substring(i+4, href.indexOf("&", i))
  }

}