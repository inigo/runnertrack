package controllers

import net.surguy.runnertrack.enrich.{EnrichRunner, LinearFinishTimePredictor}
import net.surguy.runnertrack.model.{Race, Distance, Distances}
import net.surguy.runnertrack.scraper.{CopenhagenMarathon2014Scraper, LondonMarathon2015Scraper, ManchesterMarathon2015Scraper, RaceScraper}
import play.api.mvc.{Action, Controller}

object RaceController extends Controller {
  def showRunners(raceId: String, ids: String) =  Action { implicit request =>
    val race = RaceLookup.lookupId(raceId)
    val runnerIds = ids.split(",").toSeq
    val runners = runnerIds.map( race.scraper.scrape )
    val enricher = new EnrichRunner(new LinearFinishTimePredictor(race.distance))
    val richRunners = runners.map( enricher.enrichRunner )
    Ok(views.html.runners(race.name, richRunners.flatten))
  }

}

object RaceLookup {
  def lookupId(raceId: String) = {
    val browser = RaceScraper.browser()
    raceId match {
      case "manchester2015" => Race("Greater Manchester Marathon 2015", new ManchesterMarathon2015Scraper(browser), Distances.Marathon)
      case "london2015" => Race("London Marathon 2015", new LondonMarathon2015Scraper(browser), Distances.Marathon)
      case "copenhagen2014" => Race("Copenhagen 2014", new CopenhagenMarathon2014Scraper(browser), Distances.Marathon)
    }
  }
}

