package models.errors

enum TradingError:
  case InsufficientCash
  case InsufficientHoldings
  case InvalidQuantity
  case UnsupportedSymbol(symbol: String)
  case PriceUnavailable(symbol: String)
  case UserNotFound