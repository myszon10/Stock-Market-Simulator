package controllers

import controllers.actions.AuthenticatedAction
import controllers.actions.UserRequest
import services.MarketDataServiceFactory
import org.scalatestplus.play.PlaySpec
import models.Quote
import repositories.PriceCacheRepository
import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.GET
import play.api.test.Helpers.NOT_FOUND
import play.api.test.Helpers.OK
import play.api.test.Helpers.SERVICE_UNAVAILABLE
import play.api.test.Helpers.contentAsJson
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.Helpers.status
import play.api.test.Helpers.stubControllerComponents

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class StockControllerSpec extends PlaySpec:
    private val cc = stubControllerComponents()

    private val authAction = new AuthenticatedAction(cc.parsers) {
        override protected def refine[A](request: play.api.mvc.Request[A]): Future[Either[Result, UserRequest[A]]] = {
            Future.successful(Right(new UserRequest(1L, "testUser", request)))
        }
    }
    
    private class EmptyPriceCacheRepository extends PriceCacheRepository(null)(using global):
        override def findBySymbol(symbol: String): Future[Option[Quote]] =
            Future.successful(None)
            
        override def upsert(quote: Quote): Future[Int] =
            Future.successful(1)

    private def controllerWith(configuration: Configuration): StockController =
        new StockController(
            cc,
            authAction,
            new MarketDataServiceFactory(
                configuration,
                new EmptyPriceCacheRepository()
            )
        )

    private def configuration(values: (String, String)*): Configuration =
        Configuration.from(values.toMap)

    "StockController stocks" should {
        "return list of supported stocks" in {
            val controller = controllerWith(
                configuration("marketData.mode" -> "mock")
            )

            val result = controller.stocks().apply(FakeRequest(GET, "/api/stocks"))

            status(result) mustBe OK

            val json = contentAsJson(result)
            val stocks = json.as[Seq[JsValue]]
            val symbols = stocks.map(stock => (stock \ "symbol").as[String])

            stocks.size mustBe 10
            symbols must contain("AAPL")
            symbols must contain("MSFT")
            symbols must contain("GOOGL")
            symbols must contain("AMZN")
            symbols must contain("TSLA")
        }
    }

    "StockController quote" should {
        "return quote for supported symbol in mock mode" in {
            val controller = controllerWith(
                configuration("marketData.mode" -> "mock")
            )

            val result = controller.quote("AAPL").apply(FakeRequest(GET, "/api/stocks/AAPL/quote"))

            status(result) mustBe OK

            val json = contentAsJson(result)

            (json \ "symbol").as[String] mustBe "AAPL"
            (json \ "price").as[BigDecimal] mustBe BigDecimal("182.45")
            (json \ "fetchedAt").as[String] must not be empty
        }

        "return quote for symbol written in lowercase in mock mode" in {
            val controller = controllerWith(
                configuration("marketData.mode" -> "mock")
            )

            val result = controller.quote("aapl").apply(FakeRequest(GET, "/api/stocks/aapl/quote"))

            status(result) mustBe OK

            val json = contentAsJson(result)

            (json \ "symbol").as[String] mustBe "AAPL"
            (json \ "price").as[BigDecimal] mustBe BigDecimal("182.45")
        }

        "return 404 for unsupported symbol in mock mode" in {
            val controller = controllerWith(
                configuration("marketData.mode" -> "mock")
            )

            val result = controller.quote("XYZ").apply(FakeRequest(GET, "/api/stocks/XYZ/quote"))

            status(result) mustBe NOT_FOUND

            val json = contentAsJson(result)

            (json \ "error").as[String] mustBe "UNSUPPORTED_SYMBOL"
            (json \ "message").as[String] mustBe "Symbol XYZ is not supported."
        }

        "use mock mode by default when market data mode is not configured" in {
            val controller = controllerWith(
                configuration()
            )

            val result = controller.quote("AAPL").apply(FakeRequest(GET, "/api/stocks/AAPL/quote"))

            status(result) mustBe OK

            val json = contentAsJson(result)

            (json \ "symbol").as[String] mustBe "AAPL"
            (json \ "price").as[BigDecimal] mustBe BigDecimal("182.45")
        }

        "fall back to mock mode when market data mode is unknown" in {
            val controller = controllerWith(
                configuration("marketData.mode" -> "invalid-mode")
            )

            val result = controller.quote("AAPL").apply(FakeRequest(GET, "/api/stocks/AAPL/quote"))

            status(result) mustBe OK

            val json = contentAsJson(result)

            (json \ "symbol").as[String] mustBe "AAPL"
            (json \ "price").as[BigDecimal] mustBe BigDecimal("182.45")
        }

        "use Finnhub mode when market data mode is finnhub" in {
            val controller = controllerWith(
                configuration("marketData.mode" -> "finnhub")
            )

            val result = controller.quote("AAPL").apply(FakeRequest(GET, "/api/stocks/AAPL/quote"))

            status(result) mustBe SERVICE_UNAVAILABLE

            val json = contentAsJson(result)

            (json \ "error").as[String] mustBe "MISSING_API_KEY"
            (json \ "message").as[String] mustBe "Market data API key is not configured."
        }

        "return readable 503 JSON when quote is not available from Finnhub" in {
            val controller = controllerWith(
                configuration(
                    "marketData.mode" -> "finnhub",
                    "finnhub.apiKey" -> "test-api-key"
                )
            )

            val result = controller.quote("AAPL").apply(FakeRequest(GET, "/api/stock/AAPL/quote"))

            status(result) mustBe SERVICE_UNAVAILABLE

            val json = contentAsJson(result)

            (json \ "error").as[String] must not be empty
            (json \ "message").as[String] must not be empty
        }
    }