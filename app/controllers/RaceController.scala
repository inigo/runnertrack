package controllers

import net.surguy.runnertrack.enrich.{EnrichRunner, LinearFinishTimePredictor}
import net.surguy.runnertrack.model.{Distances, Race}
import net.surguy.runnertrack.scraper._
import play.api.mvc.{Action, Controller}

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Display results for a set of runners.
 */
object RaceController extends Controller {
  // Note that this is using the Scala concurrent Duration and TimeUnit, in contrast to the rest of the code
  // which is using the Java 8 Duration and TimeUnit
  val MAX_WAIT = scala.concurrent.duration.Duration(10, scala.concurrent.duration.SECONDS)

  def showRunners(raceId: String, ids: String) =  Action.async { implicit request =>
    val race = RaceLookup.lookupId(raceId)
    val enricher = new EnrichRunner(new LinearFinishTimePredictor(race.distance))

    val runnerIds = ids.split(",").toSeq
    val runnerFutures = for (r <- runnerIds) yield {
      Future {
        val runner = race.scraper.tryScrape(RaceScraper.browser())(r)
        val result = runner.map( enricher.enrichRunner ).flatten
        result
      }
    }
    val resolvedFutures = Future { runnerFutures.map( r => Await.result(r, MAX_WAIT) ) }
    resolvedFutures.map( richRunners => Ok(views.html.runners(race.name, richRunners.flatten)))
  }

  def listRaces() = Action { implicit request =>
    Ok(views.html.listRaces(RaceLookup.raceNames))
  }

  def addRunners(raceId: String) = Action { implicit request =>
    val raceName = RaceLookup.lookupId(raceId).name
    Ok(views.html.addRunners(raceId, raceName))
  }

}

/**
 * Convert a race name passed in to the browser to a name and associated code.
 */
object RaceLookup {
  val browser = RaceScraper.browser()

  private val races = Map(
    "manchester2015" -> Race("Greater Manchester Marathon 2015", new ManchesterMarathon2015Scraper(), Distances.Marathon)
    , "london2015" -> Race("London Marathon 2015", new LondonMarathon2015Scraper(), Distances.Marathon)
    , "copenhagen2014" -> Race("Copenhagen 2014", new CopenhagenMarathon2014Scraper(), Distances.Marathon)
  ).map((kv: (String, Race)) => (kv._1, wrapWithCache(kv._2)) )

  private def wrapWithCache(r: Race) = r.copy(scraper = new CachingScraper(r.scraper))

  def lookupId(raceId: String): Race = races(raceId)
  def raceNames: Seq[(String, String)] = races.map((tuple: (String, Race)) => (tuple._1, tuple._2.name) ).toSeq
}

