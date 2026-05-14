package controllers

import controllers.actions.AuthenticatedAction
import models.Transaction
import models.errors.TradingError
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.mvc.AbstractController
import play.api.mvc.Action
import play.api.mvc.ControllerComponents
import play.api.mvc.Result
import repositories.HoldingRepository
import repositories.TransactionRepository
import repositories.UserRepository
import services.MarketDataServiceFactory
import services.TradingService

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class OrderController @Inject() (
                                cc: ControllerComponents,
                                authenticatedAction: AuthenticatedAction,
                                marketDataServiceFactory: MarketDataServiceFactory,
                                userRepository: UserRepository,
                                holdingRepository: HoldingRepository,
                                transactionRepository: TransactionRepository
                                )(using ec: ExecutionContext) extends AbstractController(cc):

    private val tradingService = new TradingService(
        marketDataService = marketDataServiceFactory.get(),
        userRepository = userRepository,
        holdingRepository = holdingRepository,
        transactionRepository = transactionRepository
    )

    def buy(): Action[JsValue] = authenticatedAction.async(parse.json) { request =>
        val symbolResult = (request.body \ "symbol").validate[String]
        val quantityResult = (request.body \ "quantity").validate[Int]

        (symbolResult.asOpt, quantityResult.asOpt) match {
            case (Some(symbol), Some(quantity)) =>
                tradingService.buy(request.userId, symbol, quantity).map {
                    case Right(transaction) =>
                        Created(transactionToJson(transaction))

                    case Left(error) =>
                        tradingErrorToResult(error)
                }

            case _ =>
                Future.successful(
                    BadRequest(
                        Json.obj(
                            "error" -> "INVALID_ORDER_REQUEST",
                            "message" -> "Request must contain symbol and quantity."
                        )
                    )
                )
        }
    }

    private def transactionToJson(transaction: Transaction): JsValue =
        val total = transaction.price * BigDecimal(transaction.quantity)

        Json.obj(
            "transactionId" -> transaction.id,
            "symbol" -> transaction.symbol,
            "side" -> transaction.side.toString.toUpperCase,
            "quantity" -> transaction.quantity,
            "price" -> transaction.price,
            "total" -> total,
            "createdAt" -> transaction.createdAt.toString
        )

    private def tradingErrorToResult(error: TradingError): Result =
        error match {
            case TradingError.InvalidQuantity =>
                BadRequest(
                    Json.obj(
                        "error" -> "INVALID_QUANTITY",
                        "message" -> "Quantity must be greater than zero"
                    )
                )

            case TradingError.InsufficientCash =>
                BadRequest(
                    Json.obj(
                        "error" -> "INSUFFICIENT_CASH",
                        "message" -> "User does not have enough cash to complete this order."
                    )
                )

            case TradingError.UnsupportedSymbol(symbol) =>
                NotFound(
                    Json.obj(
                        "error" -> "UNSUPPORTED_SYMBOL",
                        "message" -> s"Symbol $symbol is not supported."
                    )
                )

            case TradingError.PriceUnavailable(symbol) =>
                ServiceUnavailable(
                    Json.obj(
                        "error" -> "PRICE_UNAVAILABLE",
                        "message" -> s"Price for symbol $symbol is not available."
                    )
                )

            case TradingError.UserNotFound =>
                Unauthorized(
                    Json.obj(
                        "error" -> "USER_NOT_FOUND",
                        "message" -> "Authenticated user could not be found."
                    )
                )

            case TradingError.InsufficientHoldings =>
                BadRequest(
                    Json.obj(
                        "error" -> "INSUFFICIENT_HOLDINGS",
                        "message" -> "User does not have enough shares to complete this order."
                    )
                )
        }

