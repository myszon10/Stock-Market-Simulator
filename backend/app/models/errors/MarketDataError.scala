package models.errors

enum MarketDataError:
  case UnsupportedSymbol(symbol: String)
  case QuoteNotAvailable(symbol: String)
  case ExternalServiceUnavailable
  case MissingApiKey