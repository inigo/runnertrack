package net.surguy.runnertrack.scraper

import net.surguy.runnertrack.model.{Distance, Distances, Km}
import org.specs2.mutable.Specification

class DistanceParserSpec extends Specification {
  def parse(s: String) = new GenericDistanceParser().parseDistance(s)

  "Parsing distances" should {
    "work for km" in { parse("10 km") mustEqual Distance(10, Km) }
    "work for kilometres" in { parse("10 kilometres") mustEqual Distance(10, Km) }
    "work for fractions" in { parse("10.4532 km") mustEqual Distance(10.4532, Km) }
    "work for miles" in { parse("10.4532 km") mustEqual Distance(10.4532, Km) }
    "work for 'half'" in { parse("Half") mustEqual Distances.HalfMarathon }
    "work for 'finish'" in { parse("Finish") mustEqual Distances.Marathon }
  }

 }
