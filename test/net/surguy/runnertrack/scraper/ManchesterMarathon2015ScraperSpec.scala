package net.surguy.runnertrack.scraper

import java.io.File
import java.time.Duration

import net.surguy.runnertrack.model._
import org.specs2.mutable.Specification

class ManchesterMarathon2015ScraperSpec extends Specification {

  def parse(fileName: String): Runner = {
    val localUrl = new File("test/resources/"+fileName).toURI.toURL
    val browser = RaceScraper.browser()
    val scraper = new ManchesterMarathon2015Scraper(browser)
    browser.navigate().to(localUrl)
    scraper.parse
  }

  "Retrieving data for a runner who has finished" should {
    val result = parse("Inigo_Manchester2015.html")
    "return their name" in { result.name mustEqual "Inigo SURGUY"  }
    "return their club" in { result.club mustEqual "Headington Road Runners"  }
    "return their start time" in { result.startTime.get.toString mustEqual "10:00"  }
    "return their finish time" in { result.finish.get.time.toString mustEqual "PT3H6M35S"  }
    "return their finish position" in { result.finish.get.place mustEqual 691  }
    "return their splits" in { result.splits.length mustEqual 6 }
    "return the correct values for the first split" in { result.splits.head mustEqual Split(Distance(7D, Km), Duration.parse("PT30M46S")) }
    "return the correct values for the final split" in { result.splits.last mustEqual Split(Distances.Marathon, Duration.parse("PT3H06M35S")) }
  }


  "Retrieving data for a runner who did not finish" should {
    val result = parse("DNF_Manchester_2015.html")
    "return their name" in { result.name mustEqual "Example NONFINISHER"  }
    "return a start time" in { result.startTime.get.toString mustEqual "10:00"  }
    "return no finish time" in { result.finish must beNone  }
    "return some splits" in { result.splits.length mustEqual 4 }
  }


}
