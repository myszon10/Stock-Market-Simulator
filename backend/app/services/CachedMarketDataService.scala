package services

import models.Quote
import models.errors.MarketDataError
import repositories.PriceCacheRepository

import java.time.Clock
import java.util.Locale
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.concurrent.duration.FiniteDuration
import scala.util.control.NonFatal

class CachedMarketDataService(
                             delegate: MarketDataService,
                             priceCacheRepository: PriceCacheRepository,
                             ttl: FiniteDuration = 60.seconds,
                             clock: Clock = Clock.systemUTC()
                             )(using ec: ExecutionContext) extends MarketDataService:
    
    override def getQuote(symbol: String): Future[Either[MarketDataError, Quote]] =
        val normalizedSymbol = normalizeSymbol(symbol)
        
        priceCacheRepository
          .findBySymbol(normalizedSymbol)
          .recover {
              case NonFatal(_) => None
          }
          .flatMap {
              case Some(cachedQuote) if isFresh(cachedQuote) =>
                  Future.successful(Right(cachedQuote))

              case _ =>
                  fetchAndCache(normalizedSymbol)
          }
        
    private def fetchAndCache(symbol: String): Future[Either[MarketDataError, Quote]] =
        delegate.getQuote(symbol).flatMap {
            case Right(quote) =>
                priceCacheRepository
                    .upsert(quote)
                    .map(_ => Right(quote): Either[MarketDataError, Quote])
                    .recover {
                        case NonFatal(_) => Right(quote)
                    }

            case Left(error) =>
                Future.successful(Left(error): Either[MarketDataError, Quote])
        }
        
    private def isFresh(quote: Quote): Boolean =
        val ageMillis = java.time.Duration
          .between(quote.fetchedAt, clock.instant())
          .toMillis
        
        ageMillis >= 0 && ageMillis <= ttl.toMillis
        
    private def normalizeSymbol(symbol: String): String =
        symbol.trim.toUpperCase(Locale.ROOT)