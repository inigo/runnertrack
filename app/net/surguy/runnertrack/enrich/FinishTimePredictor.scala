package net.surguy.runnertrack.enrich

import java.time.Duration
import net.surguy.runnertrack.model._

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

/** Work out the finish time, assuming that their average page continues to change at the same rate. */
class RegressionFinishTimePredictor(finalDistance: Distance) extends FinishTimePredictor(finalDistance) {
  import org.apache.commons.math3.stat.regression.SimpleRegression

  override def predictFinish(richSplits: Seq[RichSplit]): Duration = {
    richSplits.length match {
      case len if len < 3 => new LinearFinishTimePredictor(finalDistance).predictFinish(richSplits)
      case _ =>
        val data = richSplits.map( d => (d.base.distance.toMetres, d.paceSoFar.toSecondsPerMetre ))
        val regression = new SimpleRegression()
        data.foreach( d => regression.addData(d._1, d._2) )
        val avgPaceAtFinishMetresPerSecond = regression.predict(Distances.Marathon.toMetres)
        val finishTimeSeconds = avgPaceAtFinishMetresPerSecond * Distances.Marathon.toMetres
        Duration.ofSeconds(finishTimeSeconds.toLong)
    }
  }
}

class EnrichRunner(predictor: FinishTimePredictor) {
  def enrichRunner(runner: Runner): Option[RichRunner] = {
    runner.startTime match {
      case Some(startTime) if runner.splits.nonEmpty =>
        val richSplits = EnrichSplits.enrichSplits(runner.splits, startTime)
        val predictedFinish = predictor.predictFinish(richSplits)
        val predictedFinishTimeOfDay = startTime.plus(predictedFinish)
        Some(RichRunner(runner, richSplits, predictedFinish, predictedFinishTimeOfDay))
      case _ => None
    }
  }
}