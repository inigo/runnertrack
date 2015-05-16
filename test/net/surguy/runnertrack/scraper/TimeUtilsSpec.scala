package net.surguy.runnertrack.scraper

import java.time.Duration

import net.surguy.runnertrack.TimeUtils
import org.specs2.mutable.Specification
import TimeUtils._

class TimeUtilsSpec extends Specification {

  "Parsing durations" should {
    "parse short durations in minutes" in { tryParseDuration("12:34") mustEqual Some(Duration.parse("PT12M34S")) }
    "parse durations in minutes" in { tryParseDuration("34:56") mustEqual Some(Duration.parse("PT34M56S")) }
    "parse durations with hours" in { tryParseDuration("2:34:56") mustEqual Some(Duration.parse("PT2H34M56S")) }
    "parse long durations with hours" in { tryParseDuration("12:34:56") mustEqual Some(Duration.parse("PT12H34M56S")) }
    "parse durations with leading zeroes" in { tryParseDuration("00:30:46") mustEqual Some(Duration.parse("PT30M46S")) }
  }

}
