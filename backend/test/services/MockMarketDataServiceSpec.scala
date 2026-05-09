package services

import models.errors.MarketDataError
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.ExecutionContext.Implicits.global

class MockMarketDataServiceSpec extends AnyWordSpec with Matchers:
    private val service = MockMarketDataService()

    "MockMarketDataService" should {
        "return quote for supported symbol" in {
            service.getQuote("AAPL").map {
                case Right(quote) =>
                    quote.symbol mustBe "AAPL"
                    quote.price mustBe BigDecimal("182.45")

                case Left(error) =>
                    fail(s"Expected quote, got error: $error")
            }
        }

        "normalize symbol before returning quote" in {
            service.getQuote(" aapl ").map {
                case Right(quote) =>
                    quote.symbol mustBe "AAPL"
                    quote.price mustBe BigDecimal("182.45")

                case Left(error) =>
                    fail(s"Expected quote, got error: $error")
            }
        }

        "return error for unsupported symbol" in {
            service.getQuote("XYZ").map { result =>
              result mustBe Left(MarketDataError.UnsupportedSymbol("XYZ"))
            }
        }
    }