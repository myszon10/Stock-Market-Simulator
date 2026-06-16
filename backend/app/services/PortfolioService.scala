package services

import models.Portfolio
import models.PortfolioPosition
import models.errors.MarketDataError
import models.errors.TradingError
import repositories.HoldingRepository
import repositories.UserRepository

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class PortfolioService(
                        marketDataService: MarketDataService,
                        userRepository: UserRepository,
                        holdingRepository: HoldingRepository
                      )(implicit ec: ExecutionContext) {

  def getPortfolio(userId: Long): Future[Either[TradingError, Portfolio]] = {
    userRepository.findById(userId).flatMap {
      case None =>
        Future.successful(Left(TradingError.UserNotFound))

      case Some(user) =>
        holdingRepository.findByUserId(userId).flatMap { holdings =>
          if (holdings.isEmpty) {
            Future.successful(Right(Portfolio(
              userId = userId,
              cashBalance = user.cashBalance,
              positions = List.empty,
              totalStockValue = BigDecimal(0),
              totalAccountValue = user.cashBalance
            )))
          } else {
            val positionFutures: List[Future[Option[PortfolioPosition]]] = holdings.map { holding =>
              marketDataService.getQuote(holding.symbol).map {
                case Right(quote) =>
                  val currentValue = quote.price * BigDecimal(holding.quantity)
                  val costBasis = holding.averageBuyPrice * BigDecimal(holding.quantity)
                  val profitLoss = currentValue - costBasis

                  Some(PortfolioPosition(
                    symbol = holding.symbol,
                    quantity = holding.quantity,
                    averageBuyPrice = holding.averageBuyPrice,
                    currentPrice = quote.price,
                    currentValue = currentValue,
                    profitLoss = profitLoss
                  ))

                case Left(_) =>
                  // If price is unavailable, skip this position
                  None
              }
            }

            Future.sequence(positionFutures).map { maybePositions =>
              val positions = maybePositions.flatten
              val totalStockValue = positions.map(_.currentValue).foldLeft(BigDecimal(0))(_ + _)
              val totalAccountValue = user.cashBalance + totalStockValue

              Right(Portfolio(
                userId = userId,
                cashBalance = user.cashBalance,
                positions = positions,
                totalStockValue = totalStockValue,
                totalAccountValue = totalAccountValue
              ))
            }
          }
        }
    }
  }
}
