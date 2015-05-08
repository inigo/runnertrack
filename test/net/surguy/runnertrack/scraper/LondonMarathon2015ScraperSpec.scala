package net.surguy.runnertrack.scraper

import java.io.File
import java.time.Duration

import net.surguy.runnertrack.model._
import org.specs2.mutable.Specification

class LondonMarathon2015ScraperSpec extends Specification {

  // Paula's ID:
  // val id = "9999990F5ECC830000171324"

  def parse(fileName: String): Runner = {
    val localUrl = new File("test/resources/"+fileName).toURI.toURL
    val browser = RaceScraper.browser()
    val scraper = new LondonMarathon2015Scraper(browser)
    browser.navigate().to(localUrl)
    scraper.parse
  }

  "Retrieving data for a runner who has finished" should {
    val result = parse("Paula_Radcliffe_London2015.html")
    "return their name" in { result.name mustEqual "Radcliffe, Paula (GBR)"  }
    "return their club" in { result.club mustEqual "BEDFORD & COUNTY AC"  }
    "return their start time" in { result.startTime.get.toString mustEqual "10:10:06"  }
    "return their finish time" in { result.finish.get.time.toString mustEqual "PT2H36M55S"  }
    "return their finish position" in { result.finish.get.place mustEqual 203  }
    "return their splits" in { result.splits.length mustEqual 10 }
    "return the correct values for the first split" in { result.splits.head mustEqual Split(Distance(5D, Km), Duration.parse("PT17M27S")) }
    "return the correct values for the final split" in { result.splits.last mustEqual Split(Distances.Marathon, Duration.parse("PT2H36M55S")) }
  }

  "Retrieving data for a runner who did not start" should {
    val result = parse("DNS_London2015.html")
    "return their name" in { result.name mustEqual "Example, Nonstarter (GBR)"  }
    "return no start time" in { result.startTime must beNone  }
    "return no finish time" in { result.finish must beNone  }
    "return no splits" in { result.splits.length mustEqual 0 }
  }

}
