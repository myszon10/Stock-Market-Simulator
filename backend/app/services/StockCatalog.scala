package services

import models.Stock

import java.util.Locale

object StockCatalog:
    private val stocks: List[Stock] = List(
        Stock("AAPL", "Apple Inc."),
        Stock("MSFT", "Microsoft Corporation"),
        Stock("GOOGL", "Alphabet Inc."),
        Stock("AMZN", "Amazon.com Inc."),
        Stock("TSLA", "Tesla Inc."),
        Stock("NVDA", "NVIDIA Corporation"),
        Stock("META", "Meta Platforms Inc."),
        Stock("NFLX", "Netflix Inc."),
        Stock("JPM", "JPMorgan Chase & Co."),
        Stock("V", "Visa Inc.")
    )

    private val supportedSymbols: Set[String] = stocks.map(stock => stock.symbol).toSet

    def all: List[Stock] = stocks

    def isSupported(symbol: String): Boolean = supportedSymbols.contains(normalizeSymbol(symbol))

    private def normalizeSymbol(str: String): String = str.trim.toUpperCase(Locale.ROOT)