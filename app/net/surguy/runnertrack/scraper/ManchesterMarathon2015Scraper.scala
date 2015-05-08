package net.surguy.runnertrack.scraper

import net.surguy.runnertrack.model._
import org.openqa.selenium.{By, WebDriver}
import scala.collection.JavaConversions._
import RaceScraper._

class ManchesterMarathon2015Scraper(val browser: WebDriver) extends RaceScraper with WebDriverTools {
  val distanceParser = new GenericDistanceParser()

  // Me: 513432
  val baseUrl = "http://www.chiprace.co.uk/MyResults.aspx?CId=38&RId=932&EId=1&AId=%s"

  override def scrape(runnerId: String) = {
    browser.navigate().to(baseUrl.format(runnerId))
    parse
  }

  def parse: Runner = {
    val name = $("#ctl00_Content_Main_lblName")
    val club = $x("//*[@id='ctl00_Content_Main_grdBio']//tr[5]/td[2]")
    val startTime = tryParseTime("10:00:00")

    val placeText = $("#ctl00_Content_Main_lblNetOPos")
    val place = if (placeText.indexOf("/")>0) placeText.substring(0, placeText.indexOf("/")).trim.toInt else -1
    val finishTime = tryParseDuration($("#ctl00_Content_Main_lblNetTime"))
    val finish = if (place != -1) finishTime.map( t => Finish(place, t)) else None

    val splits = for (row <- browser.findElements(By.xpath("//tr[@class='dxgvDataRow' and contains(@id,'grdSplits')]"))) yield {
      val distanceText = $x("td[1]", row)
      val timeText = $x("td[2]", row)
      val duration = tryParseDuration(timeText)
      duration.map( d => Split(distanceParser.parseDistance(distanceText), d) )
    }

    Runner(name, splits.flatten, club, startTime, finish)
  }

}
