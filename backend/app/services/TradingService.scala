package services

import models.Holding
import models.Transaction
import models.TransactionSide
import models.errors.MarketDataError
import models.errors.TradingError
import repositories.HoldingRepository
import repositories.TransactionRepository
import repositories.UserRepository

import java.time.Instant
import java.util.Locale
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

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
    else {
        val normalizedSymbol = normalizeSymbol(symbol)

        marketDataService.getQuote(normalizedSymbol).flatMap {
            case Left(error) =>
                Future.successful(Left(marketDataErrorToTradingError(error, normalizedSymbol)))

            case Right(quote) =>
                userRepository.findById(userId).flatMap {
                    case None =>
                        Future.successful(Left(TradingError.UserNotFound))

                    case Some(user) =>
                        val totalPrice = quote.price * BigDecimal(quantity)

                        if (user.cashBalance < totalPrice) {
                            Future.successful(Left(TradingError.InsufficientCash))
                        }
                        else {
                            val cashBalanceAfter = user.cashBalance - totalPrice

                            val transaction = Transaction(
                                id = 0L,
                                userId = userId,
                                symbol = normalizedSymbol,
                                side = TransactionSide.Buy,
                                quantity = quantity,
                                price = quote.price,
                                createdAt = Instant.now()
                            )

                            for {
                                existingHolding <- holdingRepository.findByUserAndSymbol(userId, normalizedSymbol)
                                updatedHolding = calculateUpdatedHolding(
                                    userId = userId,
                                    symbol = normalizedSymbol,
                                    quantity = quantity,
                                    price = quote.price,
                                    existingHolding = existingHolding
                                )
                                _ <- userRepository.updateCashBalance(userId, cashBalanceAfter)
                                _ <- holdingRepository.upsert(updatedHolding)
                                savedTransaction <- transactionRepository.create(transaction)
                            } yield Right(savedTransaction): Either[TradingError, Transaction]
                        }
                }
        }
    }
  }

  private def calculateUpdatedHolding(
                                     userId: Long,
                                     symbol: String,
                                     quantity: Int,
                                     price: BigDecimal,
                                     existingHolding: Option[Holding]
                                     ): Holding = {
      existingHolding match {
          case None =>
              Holding(
                  userId = userId,
                  symbol = symbol,
                  quantity = quantity,
                  averageBuyPrice = price
              )

          case Some(holding) =>
              val newQuantity = holding.quantity + quantity
              val oldTotalCost = holding.averageBuyPrice * BigDecimal(holding.quantity)
              val newTotalCost = price * BigDecimal(quantity)
              val newAverageBuyPrice = ((oldTotalCost + newTotalCost) / BigDecimal(newQuantity))
                .setScale(2, BigDecimal.RoundingMode.HALF_UP)

              Holding(
                  userId = userId,
                  symbol = symbol,
                  quantity = newQuantity,
                  averageBuyPrice = newAverageBuyPrice
              )
      }
  }

  private def marketDataErrorToTradingError(error: MarketDataError, symbol: String): TradingError = {
      error match {
          case MarketDataError.UnsupportedSymbol(unsupportedSymbol) =>
              TradingError.UnsupportedSymbol(unsupportedSymbol)

          case MarketDataError.QuoteNotAvailable(_) =>
              TradingError.PriceUnavailable(symbol)

          case MarketDataError.ExternalServiceUnavailable =>
              TradingError.PriceUnavailable(symbol)

          case MarketDataError.MissingApiKey =>
              TradingError.PriceUnavailable(symbol)
      }
  }

  private def normalizeSymbol(symbol: String): String =
      symbol.trim.toUpperCase(Locale.ROOT)
}