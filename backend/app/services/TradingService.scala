package services

import models.{Transaction, TransactionSide}
import models.errors.TradingError
import repositories.{HoldingRepository, TransactionRepository, UserRepository}

import scala.concurrent.{ExecutionContext, Future}

class TradingService(
                      marketDataService: MarketDataService,
                      userRepository: UserRepository,
                      holdingRepository: HoldingRepository,
                      transactionRepository: TransactionRepository
                    )(implicit ec: ExecutionContext) {

  def buy(userId: Long, symbol: String, quantity: Int): Future[Either[TradingError, Transaction]] = {
    if (quantity <= 0) {
      return Future.successful(Left(TradingError.InvalidQuantity))
    }

    // Miejsce na integracje w Sprincie 2.
    // - marketDataService.getQuote(symbol)
    // - userRepository.findById(userId)
    // - holdingRepository.findByUserAndSymbol(userId, symbol)
    // - transactionRepository.create(...)

    Future.successful(Left(TradingError.UnsupportedSymbol(symbol)))
  }
}