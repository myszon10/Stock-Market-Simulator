package services

import models.errors.MarketDataError
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec

import java.net.URI
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FinnhubMarketDataServiceSpec extends AnyWordSpec with Matchers with ScalaFutures:
    implicit override val patienceConfig: PatienceConfig =
        PatienceConfig(
            timeout = Span(2, Seconds),
            interval = Span(15, Millis)
        )

    private def serviceWithResponse(statusCode: Int, responseBody: String): FinnhubMarketDataService =
        FinnhubMarketDataService(
            apiKey = Some("test-api-key"),
            httpGet = (_: URI, _: String) => Future.successful(Right(statusCode -> responseBody))
        )

    private def serviceWithFailure(): FinnhubMarketDataService =
        FinnhubMarketDataService(
            apiKey = Some("test-api-key"),
            httpGet = (_: URI, _: String) => Future.successful(Left(new RuntimeException("HTTP request failed")))
        )

    "FinnhubMarketDataService" should {
        "return unsupported symbol error before checking API key" in {
            val service = FinnhubMarketDataService(apiKey = None)

            val result = service.getQuote("XYZ").futureValue

            result mustBe Left(MarketDataError.UnsupportedSymbol("XYZ"))
        }

        "return missing API key error for supported symbol when API key is not configured" in {
            val service = FinnhubMarketDataService(apiKey = None)

            val result = service.getQuote("AAPL").futureValue

            result mustBe Left(MarketDataError.MissingApiKey)
        }

        "return quote when Finnhub response contains current price" in {
            val service = serviceWithResponse(
                statusCode = 200,
                responseBody = """{"c":182.45,"d":1.2,"dp":0.66,"h":185.00,"l":180.00,"o":181.00,"pc":181.25}"""
            )

            val result = service.getQuote(" aapl ").futureValue

            result match {
                case Right(quote) =>
                    quote.symbol mustBe "AAPL"
                    quote.price mustBe BigDecimal("182.45")

                case Left(error) =>
                    fail(s"Expected quote, got error: $error")
            }
        }

        "return quote not available when Finnhub response does not contain positive current price" in {
            val service = serviceWithResponse(
                statusCode = 200,
                responseBody = """{"c":0,"d":0,"dp":0,"h":0,"l":0,"o":0,"pc":0}"""
            )

            val result = service.getQuote("AAPL").futureValue

            result mustBe Left(MarketDataError.QuoteNotAvailable("AAPL"))
        }

        "return external service unavailable when Finnhub returns non-successful status" in {
            val service = serviceWithResponse(
                statusCode = 500,
                responseBody = """{"error":"Internal server error"}"""
            )

            val result = service.getQuote("AAPL").futureValue

            result mustBe Left(MarketDataError.ExternalServiceUnavailable)
        }

        "return external service unavailable when Finnhub response is not valid JSON" in {
            val service = serviceWithResponse(
                statusCode = 200,
                responseBody = "not-json"
            )

            val result = service.getQuote("AAPL").futureValue

            result mustBe Left(MarketDataError.ExternalServiceUnavailable)
        }

        "return external service unavailable when HTTP request fails" in {
            val service = serviceWithFailure()

            val result = service.getQuote("AAPL").futureValue

            result mustBe Left(MarketDataError.ExternalServiceUnavailable)
        }

        "pass configured API key to HTTP transport" in {
            var capturedApiKey: Option[String] = None

            val service = FinnhubMarketDataService(
                apiKey = Some("test-api-key"),
                httpGet = (_: URI, apiKey: String) => {
                    capturedApiKey = Some(apiKey)
                    Future.successful(Right(200 -> """{"c":182.45}"""))
                }
            )

            val result = service.getQuote("AAPL").futureValue

            result.isRight mustBe true
            capturedApiKey mustBe Some("test-api-key")
        }
    }