package net.surguy.runnertrack.enrich

import java.time.LocalTime

import net.surguy.runnertrack.model._

/**
 * Provide additional information on the splits.
 */
object EnrichSplits {

  def enrichSplits(splits: Seq[Split], startTime: LocalTime): Seq[RichSplit] = {
    for (s <- splits) yield {
      val totalTime = s.time.toMillis/1000D
      val paceSoFar = Pace(totalTime / s.distance.value, s.distance.distanceUnit)
      RichSplit(s, startTime.plus(s.time), paceSoFar, paceSoFar)
    }
  }

}
