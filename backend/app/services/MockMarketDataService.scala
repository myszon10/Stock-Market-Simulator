package services

import models.Quote
import models.errors.MarketDataError

import java.time.Instant
import java.util.Locale
import scala.concurrent.Future

class MockMarketDataService extends MarketDataService:
    private val prices: Map[String, BigDecimal] = Map(
        "AAPL" -> BigDecimal("182.45"),
        "MSFT" -> BigDecimal("410.20"),
        "GOOGL" -> BigDecimal("175.80"),
        "AMZN" -> BigDecimal("185.30"),
        "TSLA" -> BigDecimal("175.10"),
        "NVDA" -> BigDecimal("875.25"),
        "META" -> BigDecimal("485.60"),
        "NFLX" -> BigDecimal("625.40"),
        "JPM" -> BigDecimal("198.75"),
        "V" -> BigDecimal("275.90")
    )
    
    override def getQuote(symbol: String): Future[Either[MarketDataError, Quote]] =
        val normalizedSymbol = normalizeSymbol(symbol)
        
        if StockCatalog.isSupported(normalizedSymbol) then
            prices.get(normalizedSymbol) match {
                case Some(price) =>
                    Future.successful(
                        Right(
                            Quote(
                                symbol = normalizedSymbol,
                                price = price,
                                fetchedAt = Instant.now()
                            )
                        )
                    )
                case None =>
                    Future.successful(
                        Left(MarketDataError.QuoteNotAvailable(normalizedSymbol))
                    )
            }
        else
            Future.successful(
                Left(MarketDataError.UnsupportedSymbol(normalizedSymbol))
            )

    private def normalizeSymbol(str: String): String = str.trim.toUpperCase(Locale.ROOT)        