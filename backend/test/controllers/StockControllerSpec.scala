package controllers

import controllers.actions.{AuthenticatedAction, UserRequest}
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.GET
import play.api.test.Helpers.NOT_FOUND
import play.api.test.Helpers.OK
import play.api.test.Helpers.contentAsJson
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.Helpers.status
import play.api.test.Helpers.stubControllerComponents
import utils.BaseIntegrationSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class StockControllerSpec extends BaseIntegrationSpec:
    private val configuration = Configuration.from(
        Map(
            "marketData.mode" -> "mock"
        )
    )

    private val cc = stubControllerComponents()

    private val authAction = new AuthenticatedAction(cc.parsers) {
        override protected def refine[A](request: play.api.mvc.Request[A]): Future[Either[Result, UserRequest[A]]] = {
            Future.successful(Right(new UserRequest(1L, "testUser", request)))
        }
    }

    private val controller = new StockController(cc, configuration, authAction)

    "StockController stocks" should {
        "return list of supported stocks" in {
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
        "return quote for supported symbol" in {
            val result = controller.quote("AAPL").apply(FakeRequest(GET, "/api/stocks/AAPL/quote"))

            status(result) mustBe OK

            val json = contentAsJson(result)

            (json \ "symbol").as[String] mustBe "AAPL"
            (json \ "price").as[BigDecimal] mustBe BigDecimal("182.45")
            (json \ "fetchedAt").as[String] must not be empty
        }

        "return quote for symbol written in lowercase" in {
            val result = controller.quote("aapl").apply(FakeRequest(GET, "/api/stocks/aapl/quote"))

            status(result) mustBe OK

            val json = contentAsJson(result)

            (json \ "symbol").as[String] mustBe "AAPL"
            (json \ "price").as[BigDecimal] mustBe BigDecimal("182.45")
        }

        "return 404 for unsupported symbol" in {
            val result = controller.quote("XYZ").apply(FakeRequest(GET, "/api/stocks/XYZ/quote"))

            status(result) mustBe NOT_FOUND

            val json = contentAsJson(result)

            (json \ "error").as[String] mustBe "UNSUPPORTED_SYMBOL"
            (json \ "message").as[String] mustBe "Symbol XYZ is not supported."
        }
    }