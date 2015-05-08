package net.surguy.runnertrack.model

import java.time.{Duration, LocalTime}

case class Runner(name: String, splits: Seq[Split], club: String,
                   startTime: Option[LocalTime], finish: Option[Finish])

case class Finish(place: Int, time: Duration)

case class Split(distance: Distance, time: Duration)
case class RichSplit(split: Split, timeOfDay: LocalTime, pace: Pace)

case class Distance(value: Double, distanceUnit: DistanceType)

case class Pace(seconds: Double, distanceUnit: DistanceType)

class DistanceType
case object Km extends DistanceType
case object Mile extends DistanceType

object Distances {
  val Marathon = Distance(42.195D, Km)
  val HalfMarathon = Distance(21.0975D, Km)
}