package controllers

import net.surguy.runnertrack.enrich.{EnrichRunner, LinearFinishTimePredictor}
import net.surguy.runnertrack.model.{Distances, Race}
import net.surguy.runnertrack.scraper.{CopenhagenMarathon2014Scraper, LondonMarathon2015Scraper, ManchesterMarathon2015Scraper, RaceScraper}
import play.api.mvc.{Action, Controller}

/**
 * Display results for a set of runners.
 */
object RaceController extends Controller {
  def showRunners(raceId: String, ids: String) =  Action { implicit request =>
    val race = RaceLookup.lookupId(raceId)
    val runnerIds = ids.split(",").toSeq
    val runners = runnerIds.map( race.scraper.tryScrape )
    val enricher = new EnrichRunner(new LinearFinishTimePredictor(race.distance))
    val richRunners = runners.collect{ case Some(runner) =>  enricher.enrichRunner(runner) }.flatten
    Ok(views.html.runners(race.name, richRunners))
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
    "manchester2015" -> Race("Greater Manchester Marathon 2015", new ManchesterMarathon2015Scraper(browser), Distances.Marathon)
    , "london2015" -> Race("London Marathon 2015", new LondonMarathon2015Scraper(browser), Distances.Marathon)
    , "copenhagen2014" -> Race("Copenhagen 2014", new CopenhagenMarathon2014Scraper(browser), Distances.Marathon)
  )

  def lookupId(raceId: String): Race = races(raceId)
  def raceNames: Seq[(String, String)] = races.map((tuple: (String, Race)) => (tuple._1, tuple._2.name) ).toSeq
}

