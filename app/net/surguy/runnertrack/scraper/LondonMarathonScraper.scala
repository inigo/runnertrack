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
