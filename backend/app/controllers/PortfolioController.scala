package controllers

import controllers.actions.AuthenticatedAction
import models.Portfolio
import models.PortfolioPosition
import models.errors.TradingError
import play.api.libs.json.Json
import play.api.libs.json.JsValue
import play.api.mvc.AbstractController
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.ControllerComponents
import repositories.HoldingRepository
import repositories.UserRepository
import services.MarketDataServiceFactory
import services.PortfolioService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PortfolioController @Inject()(
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

  def portfolio(): Action[AnyContent] = authenticatedAction.async { request =>
    portfolioService.getPortfolio(request.userId).map {
      case Right(p) =>
        Ok(portfolioToJson(p))

      case Left(TradingError.UserNotFound) =>
        Unauthorized(Json.obj(
          "error" -> "USER_NOT_FOUND",
          "message" -> "Authenticated user could not be found."
        ))

      case Left(_) =>
        InternalServerError(Json.obj(
          "error" -> "INTERNAL_ERROR",
          "message" -> "An unexpected error occurred."
        ))
    }
  }

  private def portfolioToJson(portfolio: Portfolio): JsValue =
    Json.obj(
      "userId" -> portfolio.userId,
      "cashBalance" -> portfolio.cashBalance,
      "positions" -> portfolio.positions.map(positionToJson),
      "totalStockValue" -> portfolio.totalStockValue,
      "totalAccountValue" -> portfolio.totalAccountValue
    )

  private def positionToJson(position: PortfolioPosition): JsValue =
    Json.obj(
      "symbol" -> position.symbol,
      "quantity" -> position.quantity,
      "averageBuyPrice" -> position.averageBuyPrice,
      "currentPrice" -> position.currentPrice,
      "currentValue" -> position.currentValue,
      "profitLoss" -> position.profitLoss
    )
