package net.surguy.runnertrack.enrich

import java.time.{Duration, LocalTime}

import net.surguy.runnertrack.model.{Distance, RichSplit, RichRunner, Runner}

/** Work out the finish time of a runner based on their splits so far. */
abstract class FinishTimePredictor(val finalDistance: Distance) {
  def predictFinish(richSplits: Seq[RichSplit]): Duration
}

/** Work out the finish time, assuming that they maintain their average pace so far. */
class LinearFinishTimePredictor(finalDistance: Distance) extends FinishTimePredictor(finalDistance) {
  override def predictFinish(richSplits: Seq[RichSplit]): Duration = {
    val averagePace = richSplits.last.paceSoFar
    val finishTimeSeconds = averagePace.seconds * (finalDistance.toMetres / averagePace.distanceUnit.toMetres(1) )
    Duration.ofSeconds(finishTimeSeconds.toLong)
  }
}

class EnrichRunner(predictor: FinishTimePredictor) {
  def enrichRunner(runner: Runner, finalDistance: Distance): Option[RichRunner] = {
    runner.startTime match {
      case Some(startTime) =>
        val richSplits = EnrichSplits.enrichSplits(runner.splits, startTime)
        val predictedFinish = predictor.predictFinish(richSplits)
        val predictedFinishTimeOfDay = startTime.plus(predictedFinish)
        Some(RichRunner(runner, richSplits, predictedFinish, predictedFinishTimeOfDay))
      case None => None
    }
  }
}