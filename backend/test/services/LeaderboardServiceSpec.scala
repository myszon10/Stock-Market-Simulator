package services

import models.Holding
import models.Quote
import models.User
import models.errors.MarketDataError
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.db.Database
import repositories.HoldingRepository
import repositories.UserRepository

import java.lang.reflect.Proxy
import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LeaderboardServiceSpec extends AnyWordSpec with Matchers with ScalaFutures {

  private val now: Instant = Instant.parse("2026-06-04T12:00:00Z")

  private val dummyDatabase: Database =
    Proxy.newProxyInstance(
      classOf[Database].getClassLoader,
      Array(classOf[Database]),
      (_, _, _) => throw new UnsupportedOperationException("Database should not be used in these tests")
    ).asInstanceOf[Database]

  private class FakeMarketDataService(prices: Map[String, BigDecimal]) extends MarketDataService {
    override def getQuote(symbol: String): Future[Either[MarketDataError, Quote]] =
      prices.get(symbol) match {
        case Some(price) =>
          Future.successful(Right(Quote(symbol = symbol, price = price, fetchedAt = now)))

        case None =>
          Future.successful(Left(MarketDataError.UnsupportedSymbol(symbol)))
      }
  }

  private class FakeUserRepository(users: List[User]) extends UserRepository(dummyDatabase)(using global) {
    override def findAll(): Future[List[User]] =
      Future.successful(users)

    override def findById(id: Long): Future[Option[User]] =
      Future.successful(users.find(_.id == id))
  }

  private class FakeHoldingRepository(
                                     holdingsByUserId: Map[Long, List[Holding]]
                                     ) extends HoldingRepository(dummyDatabase)(using global) {
    override def findByUserId(userId: Long): Future[List[Holding]] =
      Future.successful(holdingsByUserId.getOrElse(userId, List.empty))
  }

  private def leaderboardServiceWith(
                                    users: List[User],
                                    holdingsByUserId: Map[Long, List[Holding]],
                                    prices: Map[String, BigDecimal]
                                    ): LeaderboardService = {

    val userRepository = new FakeUserRepository(users)
    val holdingRepository = new FakeHoldingRepository(holdingsByUserId)
    val marketDataService = new FakeMarketDataService(prices)

    val portfolioService = new PortfolioService(
      marketDataService = marketDataService,
      userRepository = userRepository,
      holdingRepository = holdingRepository
    )(using global)

    new LeaderboardService(
      userRepository = userRepository,
      portfolioService = portfolioService
    )(using global)
  }

  "LeaderboardService.getLeaderboard" should {
    "sort users by total account value in descending order and assign ranks" in {
      val users = List(
        User(
          id = 1L,
          username = "user-low",
          passwordHash = "hash",
          cashBalance = BigDecimal("1000.00")
        ),
        User(
          id = 2L,
          username = "user-high",
          passwordHash = "hash",
          cashBalance = BigDecimal("5000.00")
        ),
        User(
          id = 3L,
          username = "user-middle",
          passwordHash = "hash",
          cashBalance = BigDecimal("3000.00")
        )
      )

      val holdingsByUserId = Map(
        1L -> List(
          Holding(
            userId = 1L,
            symbol = "AAPL",
            quantity = 1,
            averageBuyPrice = BigDecimal("100.00")
          )
        ),
        2L -> List(
          Holding(
            userId = 2L,
            symbol = "MSFT",
            quantity = 10,
            averageBuyPrice = BigDecimal("400.00")
          )
        ),
        3L -> List(
          Holding(
            userId = 3L,
            symbol = "TSLA",
            quantity = 2,
            averageBuyPrice = BigDecimal("200.00")
          )
        )
      )

      val prices = Map(
        "AAPL" -> BigDecimal("150.00"),
        "MSFT" -> BigDecimal("420.00"),
        "TSLA" -> BigDecimal("250.00")
      )

      val service = leaderboardServiceWith(
        users = users,
        holdingsByUserId = holdingsByUserId,
        prices = prices
      )

      val result = service.getLeaderboard().futureValue

      result.map(_.username) mustBe List("user-high", "user-middle", "user-low")
      result.map(_.rank) mustBe List(1, 2, 3)
      result.map(_.totalAccountValue) mustBe List(
        BigDecimal("9200.00"),
        BigDecimal("3500.00"),
        BigDecimal("1150.00")
      )
      result.map(_.profitLoss) mustBe List(
        BigDecimal("200.00"),
        BigDecimal("100.00"),
        BigDecimal("50.00")
      )
    }
  }
}