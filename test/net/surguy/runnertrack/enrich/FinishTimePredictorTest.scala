package net.surguy.runnertrack.enrich

import java.time.Duration

import net.surguy.runnertrack.model.{Distances, Mile, Distance, Split}
import net.surguy.runnertrack.scraper.RaceScraper._
import org.specs2.mutable.Specification

class FinishTimePredictorTest extends Specification {

  "predicting finish on a single value" should {
    "assume a constant pace" in {
      val richSplits = EnrichSplits.enrichSplits(Seq(Split(Distances.HalfMarathon, Duration.parse("PT120M0S") )), parseTime("10:00:00"))
      new LinearFinishTimePredictor(Distances.Marathon).predictFinish(richSplits) mustEqual Duration.parse("PT240M0S")
    }
    "match the final split, if that was for the full distance" in {
      val richSplits = EnrichSplits.enrichSplits(Seq(Split(Distances.Marathon, Duration.parse("PT180M0S") )), parseTime("10:00:00"))
      new LinearFinishTimePredictor(Distances.Marathon).predictFinish(richSplits) mustEqual Duration.parse("PT180M0S")
    }
  }

}
