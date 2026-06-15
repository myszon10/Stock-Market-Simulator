package services

import models.Portfolio
import models.User
import models.dto.leaderboard.LeaderboardEntry
import repositories.UserRepository

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class LeaderboardService(
                        userRepository: UserRepository,
                        portfolioService: PortfolioService
                        )(implicit ec: ExecutionContext) {

  private val initialBalance = BigDecimal("100000.00")

  def getLeaderboard(): Future[List[LeaderboardEntry]] = {
    userRepository.findAll().flatMap { users =>
      val entryFutures: List[Future[Option[LeaderboardEntry]]] = users.map { user =>
        createEntryForUser(user)
      }

      Future.sequence(entryFutures).map { maybeEntries =>
        maybeEntries.flatten
          .sortBy(_.totalAccountValue)(using Ordering[BigDecimal].reverse)
          .zipWithIndex
          .map { case (entry, index) =>
            entry.copy(rank = index + 1)
          }
      }
    }
  }

  private def createEntryForUser(user: User): Future[Option[LeaderboardEntry]] = {
    portfolioService.getPortfolio(user.id).map {
      case Right(portfolio) =>
        Some(
          LeaderboardEntry(
            rank = 0,
            username = user.username,
            totalAccountValue = portfolio.totalAccountValue,
            profitLoss = calculateProfitLoss(portfolio)
          )
        )

      case Left(_) =>
        None
    }
  }

  private def calculateProfitLoss(portfolio: Portfolio): BigDecimal =
    portfolio.totalAccountValue - initialBalance
}