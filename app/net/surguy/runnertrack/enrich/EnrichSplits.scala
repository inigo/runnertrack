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
      val paceSoFar = Pace(totalTime / s.distance.toMiles, Mile)

      val lastTime = splits.lastOption.map(_.base.time).getOrElse(Duration.ZERO)
      val splitTime = s.time.minus(lastTime).toMillis / 1000D
      val splitDistance = s.distance - splits.lastOption.map(_.base.distance).getOrElse(Distances.Zero)
      val splitPace = Pace(splitTime / splitDistance.toMiles, Mile)

      splits ++ Seq(RichSplit(s, startTime.plus(s.time), splitPace, paceSoFar))
    }
  }

}
