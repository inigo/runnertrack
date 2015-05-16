package net.surguy.runnertrack.scraper

import java.io.File
import java.nio.file.Files

import net.surguy.runnertrack.model._
import net.surguy.runnertrack.TimeUtils._
import org.openqa.selenium.{By, WebDriver}

import scala.collection.JavaConversions._

class CopenhagenMarathon2014Scraper(val browser: WebDriver) extends RaceScraper with WebDriverTools {
  val distanceParser = new GenericDistanceParser()

  // Julius Kiprono Mutai: 1
  val baseUrl = "http://live.ultimate.dk/desktop/front/data.php?eventid=2186&mode=participantinfo&language=us&pid=%s"

  override def scrape(runnerId: String) = {
    browser.navigate().to(baseUrl.format(runnerId))

    // The page content is a JavaScript fragment, containing the HTML within a
    // document.getElementById('PARTICIPANTINFO').innerHTML='...'; element.
    // So, we extract the HTML, and save it to a local file so Selenium can browse to it
    val source = browser.getPageSource
    val html = extractHtml(source)
    val f = File.createTempFile("runtrack", "_id"+runnerId+".html")
    Files.write(f.toPath, html.getBytes)
    try {
      browser.navigate().to(f.toURI.toURL)
      parse
    } finally {
      f.delete()
    }
  }

  private[scraper] def extractHtml(source: String): String = {
    val sourceStart = source.indexOf("innerHTML='") + "innerHTML='".length
    val sourceEnd = source.lastIndexOf("';")
    source.substring(sourceStart, sourceEnd)
  }

  def parse: Runner = {
    val name = $x("(//table[@class='participant_table_data']//span[@class='participant_value_big'])[1]")
    val club = ""

    val startTimeText = $x("(//table[@class='participant_table_data'][6])//table[1]//tr[2]//td[2]")
    val startTime = tryParseTime(startTimeText)

    val finishTimeText = $x("(//table[@class='participant_table_data'][6])//table[1]//tr[3]//td[2]")
    val finishTime = tryParseDuration(finishTimeText.split(" ").headOption.getOrElse(""))
    val placeText = $x("((//table[@class='participant_table_data'][6])//table)[2]//tr[1]//td[2]")
    val place = if (placeText.contains("of")) placeText.substring(0,placeText.indexOf(" ")).toInt else -1
    val finish = finishTime.map( t => Finish(place, t))

    val splits = for (row <- browser.findElements(By.xpath("((//table[@class='participant_table_data'][7])//table)//tr[td/@class='split_time']"))) yield {
      val distanceText = $x("td[1]", row)
      val timeText = $x("td[2]", row)
      val duration = tryParseDuration(timeText)
      duration.map( d => Split(distanceParser.parseDistance(distanceText), d) )
    }

    Runner(name, splits.flatten, club, startTime, finish)
  }

}
