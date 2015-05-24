package net.surguy.runnertrack.enrich

import java.time.Duration

import net.surguy.runnertrack.TimeUtils._
import net.surguy.runnertrack.model.{Km, Distance, Distances, Split}
import org.specs2.mutable.Specification

class FinishTimePredictorSpec extends Specification {
  private val linearPredictor = new LinearFinishTimePredictor(Distances.Marathon)
  private val regressionPredictor = new RegressionFinishTimePredictor(Distances.Marathon)

  "predicting finish on a single value for the linear predictor" should {
    "assume a constant pace" in {
      val richSplits = EnrichSplits.enrichSplits(Seq(Split(Distances.HalfMarathon, Duration.parse("PT120M0S") )), parseTime("10:00:00"))
      linearPredictor.predictFinish(richSplits) mustEqual Duration.parse("PT240M0S")
    }
    "match the final split, if that was for the full distance" in {
      val richSplits = EnrichSplits.enrichSplits(Seq(Split(Distances.Marathon, Duration.parse("PT180M0S") )), parseTime("10:00:00"))
      linearPredictor.predictFinish(richSplits) mustEqual Duration.parse("PT180M0S")
    }
  }

  "predicting finish on a single value for the regression predictor" should {
    "assume a constant pace" in {
      val richSplits = EnrichSplits.enrichSplits(Seq(Split(Distances.HalfMarathon, Duration.parse("PT120M0S") )), parseTime("10:00:00"))
      regressionPredictor.predictFinish(richSplits) mustEqual Duration.parse("PT240M0S")
    }
    "match the final split, if that was for the full distance" in {
      val richSplits = EnrichSplits.enrichSplits(Seq(Split(Distances.Marathon, Duration.parse("PT180M0S") )), parseTime("10:00:00"))
      regressionPredictor.predictFinish(richSplits) mustEqual Duration.parse("PT180M0S")
    }
  }

  "predicting finish with the regression predictor" should {
    "match the linear predictor when the pace is stable" in {
      val richSplits = EnrichSplits.enrichSplits(Seq(
          Split(Distance(5, Km), Duration.parse("PT30M0S") )
          , Split(Distance(10, Km), Duration.parse("PT60M0S") )
          , Split(Distance(15, Km), Duration.parse("PT90M0S") )
        ), parseTime("10:00:00"))
      regressionPredictor.predictFinish(richSplits) mustEqual linearPredictor.predictFinish(richSplits)
    }
    "give a predicted time based on the pace continuing to slow" in {
      val richSplits = EnrichSplits.enrichSplits(Seq(
        Split(Distance(5, Km), Duration.parse("PT22M40S") )
        , Split(Distance(10, Km), Duration.parse("PT45M24S") )
        , Split(Distance(15, Km), Duration.parse("PT68M17S") )
      ), parseTime("10:00:00"))
      regressionPredictor.predictFinish(richSplits) must beGreaterThan(linearPredictor.predictFinish(richSplits))
      regressionPredictor.predictFinish(richSplits) mustEqual Duration.parse("PT3H14M12S")
    }
  }

}
