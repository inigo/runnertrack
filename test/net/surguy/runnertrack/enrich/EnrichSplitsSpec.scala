package net.surguy.runnertrack.enrich

import java.time.Duration

import net.surguy.runnertrack.model.{Pace, Mile, Distance, Split}
import net.surguy.runnertrack.scraper.RaceScraper
import org.specs2.mutable.Specification
import RaceScraper.parseTime

class EnrichSplitsSpec extends Specification {

  "Enriching a single split" should {
    val richSplit = EnrichSplits.enrichSplits(Seq(Split(Distance(6,Mile), Duration.parse("PT60M00S") )), parseTime("10:00:00")).head
    "Provide a time of day for the split" in { richSplit.timeOfDay mustEqual parseTime("11:00:00") }
    "Give a pace for the individual split" in { richSplit.splitPace mustEqual Pace(600.0, Mile) }
    "Provide an average pace the same as the split pace" in {
      richSplit.paceSoFar mustEqual Pace(600.0, Mile)
      richSplit.paceSoFar mustEqual richSplit.splitPace
    }
  }

  "Enriching multiple splits" should {
    val richSplits = EnrichSplits.enrichSplits(Seq(
      Split(Distance(1,Mile), Duration.parse("PT5M00S") )
      ,Split(Distance(2,Mile), Duration.parse("PT10M00S") )
      ,Split(Distance(3,Mile), Duration.parse("PT20M00S") )
    ), parseTime("10:00:00"))
    "Give a pace for the individual split" in {
      richSplits.map(_.splitPace) mustEqual Seq(Pace(300.0, Mile), Pace(300.0, Mile), Pace(600.0, Mile))
    }
    "Provide an pace so far correct for each split" in {
      richSplits.map(_.paceSoFar) mustEqual Seq(Pace(300.0, Mile), Pace(300.0, Mile), Pace(400.0, Mile))
    }

  }

}
