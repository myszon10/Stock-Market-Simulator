package controllers

import controllers.actions.AuthenticatedAction
import play.api.libs.json.Json
import play.api.mvc.AbstractController
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.ControllerComponents
import repositories.HoldingRepository
import repositories.UserRepository
import services.LeaderboardService
import services.MarketDataServiceFactory
import services.PortfolioService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class LeaderboardController @Inject()(
                                     cc: ControllerComponents,
                                     authenticatedAction: AuthenticatedAction,
                                     marketDataServiceFactory: MarketDataServiceFactory,
                                     userRepository: UserRepository,
                                     holdingRepository: HoldingRepository
                                     )(using ec: ExecutionContext) extends AbstractController(cc):

  private val portfolioService = new PortfolioService(
    marketDataService = marketDataServiceFactory.get(),
    userRepository = userRepository,
    holdingRepository = holdingRepository
  )

  private val leaderboardService = new LeaderboardService(
    userRepository = userRepository,
    portfolioService = portfolioService
  )

  def leaderboard(): Action[AnyContent] = authenticatedAction.async {
    leaderboardService.getLeaderboard().map { entries =>
      Ok(Json.toJson(entries))
    }
  }