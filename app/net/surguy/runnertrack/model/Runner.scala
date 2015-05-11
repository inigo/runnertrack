package net.surguy.runnertrack.model

import java.time.{Duration, LocalTime}

case class Runner(name: String, splits: Seq[Split], club: String,
                   startTime: Option[LocalTime], finish: Option[Finish])
case class RichRunner(base: Runner, richSplits: Seq[RichSplit], predictedFinish: Duration, predictedFinishTimeOfDay: LocalTime)

case class Finish(place: Int, time: Duration)

case class Split(distance: Distance, time: Duration)
case class RichSplit(split: Split, timeOfDay: LocalTime, splitPace: Pace, paceSoFar: Pace)

case class Distance(value: Double, distanceUnit: DistanceType) {
  def toMetres = distanceUnit.toMetres(value)
}

case class Pace(seconds: Double, distanceUnit: DistanceType)

abstract class DistanceType {
  def toMetres(distance: Double): Double
}
case object Km extends DistanceType {
  override def toMetres(distanceInKm: Double) = distanceInKm * 1000
}
case object Mile extends DistanceType {
  override def toMetres(distanceInMiles: Double) = distanceInMiles * 1609.344
}

object Distances {
  val Marathon = Distance(42.195D, Km)
  val HalfMarathon = Distance(21.0975D, Km)
}