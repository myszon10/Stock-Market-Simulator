package controllers

import controllers.actions.AuthenticatedAction
import models.Quote
import models.Stock
import models.errors.MarketDataError
import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.mvc.AbstractController
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.ControllerComponents
import play.api.mvc.Result
import services.FinnhubMarketDataService
import services.MarketDataService
import services.MockMarketDataService
import services.StockCatalog

import java.util.Locale
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class StockController @Inject() (
                                  cc: ControllerComponents,
                                  configuration: Configuration,
                                  authenticatedAction: AuthenticatedAction
                                )(using ec: ExecutionContext) extends AbstractController(cc):

    private val marketDataService: MarketDataService = createMarketDataService()

    def stocks(): Action[AnyContent] = authenticatedAction {
        Ok(Json.toJson(StockCatalog.all.map(stockToJson)))
    }

    def quote(symbol: String): Action[AnyContent] = authenticatedAction.async {
        marketDataService.getQuote(symbol).map {
            case Right(quote) => Ok(quoteToJson(quote))
            case Left(error) => marketDataErrorToResult(error)
        }
    }

    private def createMarketDataService(): MarketDataService =
        val mode = configuration
          .getOptional[String]("marketData.mode")
          .getOrElse("mock")
          .trim
          .toLowerCase(Locale.ROOT)

        mode match {
            case "mock" => MockMarketDataService()
            case "finnhub" =>
                val apiKey = configuration
                    .getOptional[String]("finnhub.apiKey")
                    .map(_.trim)
                    .filter(_.nonEmpty)

                FinnhubMarketDataService(apiKey)

            case _ => MockMarketDataService()
        }

    private def stockToJson(stock: Stock): JsValue =
        Json.obj(
            "symbol" -> stock.symbol,
            "name" -> stock.name
        )

    private def quoteToJson(quote: Quote): JsValue =
        Json.obj(
            "symbol" -> quote.symbol,
            "price" -> quote.price,
            "fetchedAt" -> quote.fetchedAt.toString
        )

    private def marketDataErrorToResult(error: MarketDataError): Result =
        error match {
            case MarketDataError.UnsupportedSymbol(symbol) =>
                NotFound(
                    Json.obj(
                        "error" -> "UNSUPPORTED_SYMBOL",
                        "message" -> s"Symbol $symbol is not supported."
                    )
                )

            case MarketDataError.QuoteNotAvailable(symbol) =>
                ServiceUnavailable(
                    Json.obj(
                        "error" -> "QUOTE_NOT_AVAILABLE",
                        "message" -> s"Quote for symbol '$symbol' is not available."
                    )
                )

            case MarketDataError.ExternalServiceUnavailable =>
                ServiceUnavailable(
                    Json.obj(
                        "error" -> "EXTERNAL_SERVICE_UNAVAILABLE",
                        "message" -> "External market data service is unavailable."
                    )
                )

            case MarketDataError.MissingApiKey =>
                ServiceUnavailable(
                    Json.obj(
                        "error" -> "MISSING_API_KEY",
                        "message" -> "Market data API key is not configured."
                    )
                )
        }