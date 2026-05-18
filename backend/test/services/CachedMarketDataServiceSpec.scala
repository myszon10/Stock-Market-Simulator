package services

import models.Quote
import models.errors.MarketDataError
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.time.Millis
import org.scalatest.time.Seconds
import org.scalatest.time.Span
import org.scalatest.wordspec.AnyWordSpec
import repositories.PriceCacheRepository

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class CachedMarketDataServiceSpec extends AnyWordSpec with Matchers with ScalaFutures:
    implicit override val patienceConfig: PatienceConfig =
        PatienceConfig(
            timeout = Span(2, Seconds),
            interval = Span(15, Millis)
        )

    private val now = Instant.parse("2026-05-14T12:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    private class FakeMarketDataService(result: Either[MarketDataError, Quote]) extends MarketDataService:
        var requestedSymbols: List[String] = List.empty

        override def getQuote(symbol: String): Future[Either[MarketDataError, Quote]] =
            requestedSymbols = requestedSymbols :+ symbol
            Future.successful(result)

    private class FakePriceCacheRepository(initialQuote: Option[Quote]) extends PriceCacheRepository(null)(using global):
        var requestedSymbols: List[String] = List.empty
        var savedQuotes: List[Quote] = List.empty

        override def findBySymbol(symbol: String): Future[Option[Quote]] = {
            requestedSymbols = requestedSymbols :+ symbol
            Future.successful(initialQuote)
        }

        override def upsert(quote: Quote): Future[Int] = {
            savedQuotes = savedQuotes :+ quote
            Future.successful(1)
        }

    "CachedMarketDataService" should {
        "return fresh quote from cache without calling delegate service" in {
            val cachedQuote = Quote(
                symbol = "AAPL",
                price = BigDecimal("182.45"),
                fetchedAt = now.minusSeconds(30)
            )

            val delegateQuote = Quote(
                symbol = "AAPL",
                price = BigDecimal("999.99"),
                fetchedAt = now
            )

            val delegate = new FakeMarketDataService(Right(delegateQuote))
            val cacheRepository = new FakePriceCacheRepository(Some(cachedQuote))

            val service = CachedMarketDataService(
                delegate = delegate,
                priceCacheRepository = cacheRepository,
                ttl = 60.second,
                clock = clock
            )

            val result = service.getQuote("AAPL").futureValue

            result mustBe Right(cachedQuote)
            cacheRepository.requestedSymbols mustBe List("AAPL")
            delegate.requestedSymbols mustBe List.empty
            cacheRepository.savedQuotes mustBe List.empty
        }

        "fetch quote from delegate and save it when cache is empty" in {
            val delegateQuote = Quote(
                symbol = "AAPL",
                price = BigDecimal("190.00"),
                fetchedAt = now
            )

            val delegate = new FakeMarketDataService(Right(delegateQuote))
            val cacheRepository = new FakePriceCacheRepository(None)

            val service = CachedMarketDataService(
                delegate = delegate,
                priceCacheRepository = cacheRepository,
                ttl = 60.seconds,
                clock = clock
            )

            val result = service.getQuote(" aapl ").futureValue

            result mustBe Right(delegateQuote)
            cacheRepository.requestedSymbols mustBe List("AAPL")
            delegate.requestedSymbols mustBe List("AAPL")
            cacheRepository.savedQuotes mustBe List(delegateQuote)
        }

        "fetch quote from delegate and save it when cached quote is stale" in {
            val staleQuote = Quote(
                symbol = "AAPL",
                price = BigDecimal("182.45"),
                fetchedAt = now.minusSeconds(120)
            )

            val delegateQuote = Quote(
                symbol = "AAPL",
                price = BigDecimal("191.50"),
                fetchedAt = now
            )

            val delegate = new FakeMarketDataService(Right(delegateQuote))
            val cacheRepository = new FakePriceCacheRepository(Some(staleQuote))
            
            val service = CachedMarketDataService(
                delegate = delegate,
                priceCacheRepository = cacheRepository,
                ttl = 60.seconds,
                clock = clock
            )
            
            val result = service.getQuote("AAPL").futureValue
            
            result mustBe Right(delegateQuote)
            cacheRepository.requestedSymbols mustBe List("AAPL")
            delegate.requestedSymbols mustBe List("AAPL")
            cacheRepository.savedQuotes mustBe List(delegateQuote)
        }
        
        "return delegate error without saving anything when delegate fails" in {
            val delegate = new FakeMarketDataService(Left(MarketDataError.ExternalServiceUnavailable))
            val cacheRepository = new FakePriceCacheRepository(None)
            
            val service = CachedMarketDataService(
                delegate = delegate,
                priceCacheRepository = cacheRepository,
                ttl = 60.seconds,
                clock = clock
            )
            
            val result = service.getQuote("MSFT").futureValue
            
            result mustBe Left(MarketDataError.ExternalServiceUnavailable)
            cacheRepository.requestedSymbols mustBe List("MSFT")
            delegate.requestedSymbols mustBe List("MSFT")
            cacheRepository.savedQuotes mustBe List.empty
        }
    }