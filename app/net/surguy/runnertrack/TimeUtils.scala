package net.surguy.runnertrack

import java.time.format.DateTimeFormatter
import java.time.temporal.{TemporalAccessor, TemporalQuery}
import java.time.{LocalTime, Duration}

import scala.util.Try

object TimeUtils {

  def format(duration: Duration) = {
    val hours = duration.toHours
    val minutes = ((duration.getSeconds % (60 * 60)) / 60).asInstanceOf[Int]
    val seconds = (duration.getSeconds % 60).asInstanceOf[Int]
    f"$hours%02d:$minutes%02d:$seconds%02d"
  }

  def tryParseTime(time: String): Option[LocalTime] = Try(if (time.length>2) Some(parseTime(time)) else None).toOption.flatten
  def parseTime(time: String) = DateTimeFormatter.ISO_LOCAL_TIME.parse(time, new TemporalQuery[LocalTime] {
    override def queryFrom(temporal: TemporalAccessor) = LocalTime.from(temporal)
  })

  def tryParseDuration(time: String): Option[Duration] = Try(if (time.length>2) Some(parseDuration(time)) else None).toOption.flatten
  def parseDuration(time: String): Duration = {
    val timeSplits = time.split(":").toList.reverse
    def at(n: Int) = if (timeSplits.length>n) timeSplits(n).toInt else 0
    val seconds = at(0)
    val minutes = at(1)
    val hours = at(2)
    Duration.ofSeconds(seconds + minutes*60 + hours*3600)
  }

}
