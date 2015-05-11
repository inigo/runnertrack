package net.surguy.runnertrack.enrich

import java.time.Duration

import net.surguy.runnertrack.model.{Pace, Mile, Distance, Split}
import net.surguy.runnertrack.scraper.RaceScraper
import org.specs2.mutable.Specification
import RaceScraper.parseTime

class EnrichSplitsSpec extends Specification {

  "Enriching splits" should {
    val richSplit = EnrichSplits.enrichSplits(Seq(Split(Distance(6,Mile), Duration.parse("PT60M00S") )), parseTime("10:00:00")).head
    "Provide a time of day for the split" in { richSplit.timeOfDay mustEqual parseTime("11:00:00") }
    "Provide an average pace so far" in { richSplit.paceSoFar mustEqual Pace(600.0, Mile) }
  }

}
