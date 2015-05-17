package controllers

import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.test._

@RunWith(classOf[JUnitRunner])
class IntegrationSpec extends Specification {

  "The home page" should {
    "display a list of races" in new WithBrowser {
      browser.goTo("http://localhost:" + port)
      browser.pageSource must contain("Greater Manchester Marathon")
      browser.pageSource must contain("London Marathon")
    }
  }

  "The results page" should {
    "show runners names" in new WithBrowser {
      browser.goTo("http://localhost:" + port+"/manchester2015/513432,26148")
      val source = browser.pageSource.toLowerCase
      source must contain("Inigo Surguy".toLowerCase)
      source must contain("Paul Martelletti".toLowerCase)
    }
  }

}
