package services

import models.Quote
import models.errors.MarketDataError

import scala.concurrent.Future

trait MarketDataService:
  def getQuote(symbol: String): Future[Either[MarketDataError, Quote]]