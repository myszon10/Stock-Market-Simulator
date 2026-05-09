package services

import models.errors.MarketDataError
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.ExecutionContext.Implicits.global

class FinnhubMarketDataServiceSpec extends AnyWordSpec with Matchers:
    "FinnhubMarketDataService" should {
        "return unsupported symbol error before checking API key" in {
            val service = FinnhubMarketDataService(apiKey = None)

            service.getQuote("XYZ").map { result =>
              result mustBe Left(MarketDataError.UnsupportedSymbol("XYZ"))
            }
        }

        "return missing API key error for supported symbol when API key is not configured" in {
            val service = FinnhubMarketDataService(apiKey = None)

            service.getQuote("AAPL").map { result =>
              result mustBe Left(MarketDataError.MissingApiKey)
            }
        }

        "return external service unavailable until real Finnhub integration is implemented" in {
            val service = FinnhubMarketDataService(apiKey = Some("test-api-key"))

            service.getQuote("AAPL").map { result =>
              result mustBe Left(MarketDataError.ExternalServiceUnavailable)
            }
        }
    }