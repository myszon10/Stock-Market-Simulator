package services

import models.Quote
import models.errors.MarketDataError

import java.util.Locale
import scala.concurrent.Future

class FinnhubMarketDataService(apiKey: Option[String]) extends MarketDataService:
    override def getQuote(symbol: String): Future[Either[MarketDataError, Quote]] =
        val normalizedSymbol = normalizeSymbol(symbol)

        if !StockCatalog.isSupported(normalizedSymbol) then
            Future.successful(
                Left(MarketDataError.UnsupportedSymbol(normalizedSymbol))
            )
        else if apiKey.isEmpty then
            Future.successful(
                Left(MarketDataError.MissingApiKey)
            )
        else
            Future.successful(
                Left(MarketDataError.ExternalServiceUnavailable)
            )

    private def normalizeSymbol(str: String): String = str.trim.toUpperCase(Locale.ROOT)