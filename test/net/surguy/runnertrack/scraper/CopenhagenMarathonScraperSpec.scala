package net.surguy.runnertrack.scraper

import java.io.File
import java.nio.file.Files
import java.time.Duration

import net.surguy.runnertrack.model._
import org.specs2.mutable.Specification

class CopenhagenMarathonScraperSpec extends Specification {

  val scraper = new CopenhagenMarathonScraper(CopenhagenMarathonScraper.RACE_ID_2015)

  def parse(fileName: String): Runner = {
    val localUrl = new File("test/resources/"+fileName).toURI.toURL
    val browser = RaceScraper.browser()
    val scraper = new CopenhagenMarathonScraper("2601")
    browser.navigate().to(localUrl)
    scraper.parse(browser)
  }

  "Extracting the HTML from a JS file" should {
    "only leave the HTML" in {
      val htmlContent = new String(Files.readAllBytes(new File("test/resources/winner_html_Copenhagen_2014.html").toPath))
      val jsContent = new String(Files.readAllBytes(new File("test/resources/winner_js_Copenhagen_2014.html").toPath))
      scraper.extractHtml(jsContent) mustEqual htmlContent
    }
  }

  "Retrieving data for a runner who has finished" should {
    val result = parse("winner_html_Copenhagen_2014.html")
    "return their name" in { result.name mustEqual "Julius Kiprono Mutai" }
    "return their club" in { result.club mustEqual "Kenya (KEN)"  }
    "return their start time" in { result.startTime.get.toString mustEqual "09:30:03"  }
    "return their finish time" in { result.finish.get.time.toString mustEqual "PT2H17M54S"  }
    "return their finish position" in { result.finish.get.place mustEqual 1  }
    "return their splits" in { result.splits.length mustEqual 10 }
    "return the correct values for the first split" in { result.splits.head mustEqual Split(Distance(5D, Km), Duration.parse("PT15M52S")) }
    "return the correct values for the final split" in { result.splits.last mustEqual Split(Distances.Marathon, Duration.parse("PT2H17M54S")) }
  }


  "Retrieving data for a runner who has finished" should {
    val result = parse("inprogress_html_Copenhagen2015.html")
    "return their name" in {result.name mustEqual "Test Runner"}
    "return their club" in {result.club mustEqual "Headington Road Runners"}
  }

}
