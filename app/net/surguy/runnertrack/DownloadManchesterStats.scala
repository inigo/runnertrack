package net.surguy.runnertrack

import java.io.{Writer, File, FileWriter}
import java.util
import scala.collection.JavaConversions._

import net.surguy.runnertrack.scraper.{WebDriverTools, RaceScraper}
import org.openqa.selenium.{WebElement, By, SearchContext, WebDriver}

/**
 * Bulk download all splits from the Manchester Marathon.
 */
object DownloadManchesterStats extends App with WebDriverTools {
  val baseUrl = "http://www.chiprace.co.uk/results.aspx?CId=38&RId=932&EId=1&r=%s"

  downloadStats()

  def downloadStats() = {
    val browser = RaceScraper.browser()
    val f = new File("manchester2015.csv")
    val writer = new FileWriter(f)
    writeLine(writer, "Position", "Race no", "Name", "Chip time", "Category", "Category position", "Gender", "Gender position",
                "Club", "7km split", "10km split", "10 mile split", "Half split", "20 mile split", "Finish split")
    for (i <- 0 until 260) {
      val offset = 1 + (i * 30)
      val records = scrape(browser, baseUrl.format(""+offset))
      for (r <- records) {
        writeRecord(writer, r)
      }
      Thread.sleep(500)
      writer.flush()
    }
    writer.close()
  }

  def writeLine(w: Writer, s: String*) = w.write(s.mkString("\"","\",\"","\"")+"\n")

  def writeRecord(w: Writer, r: Record) = {
    writeLine(w, r.pos, r.raceNo, r.name, r.chipTime, r.category, r.catPos, r.gender, r.genPos, r.club,
      r.split7km, r.split10km ,r.split10m, r.splitHalf, r.split20m, r.splitEnd)
  }

  def scrape(implicit browser: WebDriver, url: String): Seq[Record] = {
    println("Scraping "+url)
    browser.navigate().to(url)
    val rows: util.List[WebElement] = browser.findElements(By.cssSelector("tr.dxgvDataRow"))
    val results = for (row <- rows) yield {
      val cells = row.findElements(By.cssSelector("td")).map( _.getText.trim ).toList
      if (cells.length==15) {
        Some(Record(cells(0), cells(1), cells(2), cells(3), cells(4), cells(5), cells(6), cells(7), cells(8), cells(9), cells(10),
          cells(11), cells(12), cells(13), cells(14)))
      } else {
        None
      }
    }
    println("Got "+results.flatten.length+" results")
    results.flatten
  }

  case class Record(pos: String, raceNo: String, name: String, chipTime: String,
                    category: String,  catPos: String,  gender: String, genPos: String,
                    club: String, split7km: String, split10km: String, split10m: String,
                    splitHalf: String, split20m: String, splitEnd: String)

}
