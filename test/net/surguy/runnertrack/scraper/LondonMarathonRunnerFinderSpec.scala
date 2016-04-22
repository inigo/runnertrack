package net.surguy.runnertrack.scraper

import java.io.File

import org.specs2.mutable.Specification

class LondonMarathonRunnerFinderSpec extends Specification {

  val finder = new LondonMarathonRunnerFinder("2015")
  val paulaRunnerId = "25371"

  def parse(fileName: String): Option[String] = {
    val localUrl = new File("test/resources/"+fileName).toURI.toURL
    val browser = RaceScraper.browser()
    browser.navigate().to(localUrl)
    finder.parse(browser)
  }

  "Extracting the identifier from an href" should {
    "return the correct id" in {
      finder.extractId("http://results-2015.virginmoneylondonmarathon.com/2015/?content=detail&fpid=search&pid=search&idp=9999990F5ECC830000171324&" +
        "lang=EN_CAP&event=MAS&search%5Bstart_no%5D=25371&search%5Bsex%5D=%25&search%5Bnation%5D=%25&search_sort=name&search_event=MAS") mustEqual "9999990F5ECC830000171324"
    }
  }

  "Finding a runner's identifier" should {
    "return the correct id" in pending("avoid hitting the live server")  {
      val browser = RaceScraper.browser()
      finder.findRunnerId(browser)(paulaRunnerId) mustEqual Some("9999990F5ECC830000171324")
    }
    "return nothing when the race number is wrong" in pending("avoid hitting the live server")  {
      val browser = RaceScraper.browser()
      finder.findRunnerId(browser)("asdasd") mustEqual None
    }
    "parse the identifier out of the search results HTML" in {
      parse("Paula_Radcliffe_RunnerSearch.html") mustEqual Some("9999990F5ECC830000171324")
    }
  }

}
