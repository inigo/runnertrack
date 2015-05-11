package net.surguy.runnertrack.enrich

import java.time.{Duration, LocalTime}

import net.surguy.runnertrack.model._

/**
 * Provide additional information on the splits.
 */
object EnrichSplits {

  def enrichSplits(splits: Seq[Split], startTime: LocalTime): Seq[RichSplit] = {
    splits.foldLeft(Seq[RichSplit]()) { (splits: Seq[RichSplit], s: Split) =>
      val totalTime = s.time.toMillis/1000D
      val paceSoFar = Pace(totalTime / s.distance.value, s.distance.distanceUnit)

      val lastTime = splits.lastOption.map(_.split.time).getOrElse(Duration.ZERO)
      val splitTime = s.time.minus(lastTime).toMillis / 1000D
      val splitDistance = s.distance - splits.lastOption.map(_.split.distance).getOrElse(Distance.ZERO)
      val splitPace = Pace(splitTime / splitDistance.value, splitDistance.distanceUnit)

      splits ++ Seq(RichSplit(s, startTime.plus(s.time), splitPace, paceSoFar))
    }
  }

}
