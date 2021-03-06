package controllers

import net.surguy.runnertrack.enrich.{EnrichRunner, RegressionFinishTimePredictor}
import net.surguy.runnertrack.model.{Distances, Race}
import net.surguy.runnertrack.scraper._
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

/**
 * Display results for a set of runners.
 */
object RaceController extends Controller {
  // Note that this is using the Scala concurrent Duration and TimeUnit, in contrast to the rest of the code
  // which is using the Java 8 Duration and TimeUnit
  val MAX_WAIT = scala.concurrent.duration.Duration(22, scala.concurrent.duration.SECONDS)

  def showRunners(raceId: String, ids: String) =  Action.async { implicit request =>
    val race = RaceLookup.lookupId(raceId)
    val enricher = new EnrichRunner(new RegressionFinishTimePredictor(race.distance))

    val runnerIds = ids.split(",").toSeq
    val runnerFutures = for (r <- runnerIds) yield {
      Future {
        val runner = race.scraper.tryScrape(RaceScraper.browser())(r)
        val result = runner.flatMap(enricher.enrichRunner)
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

  def lookupRunners(raceId: String, raceNumbers: String) = Action { implicit request =>
    val race = RaceLookup.lookupId(raceId)
    val splitRaceNumbers = raceNumbers.split(",").toSeq
    // Use the RunnerFinder to look up the runner - defaulting to the originally entered id if not found
    val ids = splitRaceNumbers.map(id => race.runnerFinder.findRunnerId(RaceScraper.browser())(id).getOrElse(id))
    Redirect(controllers.routes.RaceController.showRunners(raceId, ids.mkString(",")))
  }

}

/**
 * Convert a race name passed in to the browser to a name and associated code.
 */
object RaceLookup {
  val browser = RaceScraper.browser()

  private val races = Map(
    "manchester2015" -> Race("Greater Manchester Marathon 2015", new ManchesterMarathon2015Scraper(), Distances.Marathon, new NoopRunnerFinder())
    , "london2015" -> Race("London Marathon 2015", new LondonMarathonScraper("2015"), Distances.Marathon, new LondonMarathonRunnerFinder("2015"))
    , "london2016" -> Race("London Marathon 2016", new LondonMarathonScraper("2016"), Distances.Marathon, new LondonMarathonRunnerFinder("2016"))
    , "london2017" -> Race("London Marathon 2017", new LondonMarathonScraper("2017"), Distances.Marathon, new LondonMarathonRunnerFinder("2017"))
    , "london2018" -> Race("London Marathon 2018", new LondonMarathonScraper("2018"), Distances.Marathon, new LondonMarathonRunnerFinder("2018"))
    , "copenhagen2014" -> Race("Copenhagen 2014", new CopenhagenMarathonScraper(CopenhagenMarathonScraper.RACE_ID_2014), Distances.Marathon, new NoopRunnerFinder())
    , "copenhagen2015" -> Race("Copenhagen 2015", new CopenhagenMarathonScraper(CopenhagenMarathonScraper.RACE_ID_2015), Distances.Marathon, new NoopRunnerFinder())
  ).map((kv: (String, Race)) => (kv._1, wrapWithCache(kv._2)) )

  private def wrapWithCache(r: Race) = r.copy(scraper = new CachingScraper(r.scraper), runnerFinder = new CachingRunnerFinder(r.runnerFinder))

  def lookupId(raceId: String): Race = races(raceId)
  def raceNames: Seq[(String, String)] = races.map((tuple: (String, Race)) => (tuple._1, tuple._2.name) ).toSeq
}

