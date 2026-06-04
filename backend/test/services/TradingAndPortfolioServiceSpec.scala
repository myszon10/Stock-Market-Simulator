package services

import models.Holding
import models.Portfolio
import models.PortfolioPosition
import models.Quote
import models.Transaction
import models.TransactionSide
import models.User
import models.errors.MarketDataError
import models.errors.TradingError
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import repositories.HoldingRepository
import repositories.TransactionRepository
import repositories.UserRepository

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.api.db.Database
import java.lang.reflect.Proxy

class TradingAndPortfolioServiceSpec extends AnyWordSpec with Matchers with ScalaFutures with EitherValues {

  private val now: Instant = Instant.parse("2026-05-14T12:00:00Z")

  private val dummyDatabase: Database =
    Proxy.newProxyInstance(
      classOf[Database].getClassLoader,
      Array(classOf[Database]),
      (_, _, _) => throw new UnsupportedOperationException("Database should not be used in these tests")
    ).asInstanceOf[Database]

  // --- Fakes ---

  private class FakeMarketDataService(prices: Map[String, BigDecimal]) extends MarketDataService {
    var requestedSymbols: List[String] = List.empty

    override def getQuote(symbol: String): Future[Either[MarketDataError, Quote]] = {
      requestedSymbols = requestedSymbols :+ symbol
      prices.get(symbol) match {
        case Some(price) =>
          Future.successful(Right(Quote(symbol = symbol, price = price, fetchedAt = now)))
        case None =>
          Future.successful(Left(MarketDataError.UnsupportedSymbol(symbol)))
      }
    }
  }

  private object FakeMarketDataService {
    def single(symbol: String, price: BigDecimal): FakeMarketDataService =
      new FakeMarketDataService(Map(symbol -> price))

    def apply(prices: Map[String, BigDecimal]): FakeMarketDataService =
      new FakeMarketDataService(prices)
  }

  private class FakeUserRepository(initialUser: Option[User]) extends UserRepository(dummyDatabase)(using global) {
    var requestedUserIds: List[Long] = List.empty
    var updatedBalances: List[(Long, BigDecimal)] = List.empty

    override def findById(id: Long): Future[Option[User]] = {
      requestedUserIds = requestedUserIds :+ id
      Future.successful(initialUser)
    }

    override def updateCashBalance(userId: Long, newBalance: BigDecimal): Future[Int] = {
      updatedBalances = updatedBalances :+ (userId, newBalance)
      Future.successful(1)
    }
  }

  private class FakeHoldingRepository(
                                       initialHolding: Option[Holding] = None,
                                       initialHoldings: List[Holding] = List.empty
                                     ) extends HoldingRepository(dummyDatabase)(using global) {
    var requestedHoldings: List[(Long, String)] = List.empty
    var savedHoldings: List[Holding] = List.empty
    var deletedHoldings: List[(Long, String)] = List.empty

    override def findByUserAndSymbol(userId: Long, symbol: String): Future[Option[Holding]] = {
      requestedHoldings = requestedHoldings :+ (userId, symbol)
      Future.successful(initialHolding)
    }

    override def findByUserId(userId: Long): Future[List[Holding]] = {
      Future.successful(initialHoldings)
    }

    override def upsert(holding: Holding): Future[Unit] = {
      savedHoldings = savedHoldings :+ holding
      Future.successful(())
    }

    override def delete(userId: Long, symbol: String): Future[Unit] = {
      deletedHoldings = deletedHoldings :+ (userId, symbol)
      Future.successful(())
    }
  }

  private class FakeTransactionRepository extends TransactionRepository(dummyDatabase)(using global) {
    var savedTransactions: List[Transaction] = List.empty

    override def create(transaction: Transaction): Future[Transaction] = {
      val savedTransaction = transaction.copy(id = 1001L)
      savedTransactions = savedTransactions :+ savedTransaction
      Future.successful(savedTransaction)
    }
  }

  private def tradingServiceWith(
                                  marketDataService: MarketDataService,
                                  userRepository: UserRepository,
                                  holdingRepository: HoldingRepository,
                                  transactionRepository: TransactionRepository
                                ): TradingService =
    new TradingService(
      marketDataService,
      userRepository,
      holdingRepository,
      transactionRepository
    )(using global)

  private def portfolioServiceWith(
                                    marketDataService: MarketDataService,
                                    userRepository: UserRepository,
                                    holdingRepository: HoldingRepository
                                  ): PortfolioService =
    new PortfolioService(
      marketDataService,
      userRepository,
      holdingRepository
    )(using global)

  // ===========================================
  // TradingService.sell tests
  // ===========================================

  "TradingService.sell" should {

    "return InvalidQuantity when quantity <= 0" in {
      val mds = FakeMarketDataService.single("AAPL", BigDecimal("150.00"))
      val userRepo = new FakeUserRepository(None)
      val holdingRepo = new FakeHoldingRepository()
      val txRepo = new FakeTransactionRepository

      val service = tradingServiceWith(mds, userRepo, holdingRepo, txRepo)

      service.sell(userId = 1L, symbol = "AAPL", quantity = 0).futureValue mustBe Left(TradingError.InvalidQuantity)
      service.sell(userId = 1L, symbol = "AAPL", quantity = -5).futureValue mustBe Left(TradingError.InvalidQuantity)

      mds.requestedSymbols mustBe List.empty
      txRepo.savedTransactions mustBe List.empty
    }

    "return InsufficientHoldings when user has no holdings for the symbol" in {
      val user = User(id = 1L, username = "user1", passwordHash = "hash", cashBalance = BigDecimal("10000.00"))
      val mds = FakeMarketDataService.single("AAPL", BigDecimal("150.00"))
      val userRepo = new FakeUserRepository(Some(user))
      val holdingRepo = new FakeHoldingRepository(initialHolding = None)
      val txRepo = new FakeTransactionRepository

      val service = tradingServiceWith(mds, userRepo, holdingRepo, txRepo)
      val result = service.sell(userId = 1L, symbol = "AAPL", quantity = 1).futureValue

      result mustBe Left(TradingError.InsufficientHoldings)
      txRepo.savedTransactions mustBe List.empty
    }

    "return InsufficientHoldings when user has fewer shares than requested" in {
      val user = User(id = 1L, username = "user1", passwordHash = "hash", cashBalance = BigDecimal("10000.00"))
      val holding = Holding(userId = 1L, symbol = "AAPL", quantity = 3, averageBuyPrice = BigDecimal("140.00"))
      val mds = FakeMarketDataService.single("AAPL", BigDecimal("150.00"))
      val userRepo = new FakeUserRepository(Some(user))
      val holdingRepo = new FakeHoldingRepository(initialHolding = Some(holding))
      val txRepo = new FakeTransactionRepository

      val service = tradingServiceWith(mds, userRepo, holdingRepo, txRepo)
      val result = service.sell(userId = 1L, symbol = "AAPL", quantity = 5).futureValue

      result mustBe Left(TradingError.InsufficientHoldings)
      txRepo.savedTransactions mustBe List.empty
    }

    "successfully sell shares and update balance" in {
      val user = User(id = 1L, username = "user1", passwordHash = "hash", cashBalance = BigDecimal("5000.00"))
      val holding = Holding(userId = 1L, symbol = "AAPL", quantity = 10, averageBuyPrice = BigDecimal("140.00"))
      val mds = FakeMarketDataService.single("AAPL", BigDecimal("150.00"))
      val userRepo = new FakeUserRepository(Some(user))
      val holdingRepo = new FakeHoldingRepository(initialHolding = Some(holding))
      val txRepo = new FakeTransactionRepository

      val service = tradingServiceWith(mds, userRepo, holdingRepo, txRepo)
      val result = service.sell(userId = 1L, symbol = "AAPL", quantity = 3).futureValue

      val transaction = result.value
      transaction.id mustBe 1001L
      transaction.userId mustBe 1L
      transaction.symbol mustBe "AAPL"
      transaction.side mustBe TransactionSide.Sell
      transaction.quantity mustBe 3
      transaction.price mustBe BigDecimal("150.00")

      // Balance should increase by 150.00 * 3 = 450.00 -> 5000.00 + 450.00 = 5450.00
      userRepo.updatedBalances mustBe List((1L, BigDecimal("5450.00")))
    }

    "update holding quantity without changing averageBuyPrice after partial sell" in {
      val user = User(id = 1L, username = "user1", passwordHash = "hash", cashBalance = BigDecimal("5000.00"))
      val holding = Holding(userId = 1L, symbol = "AAPL", quantity = 10, averageBuyPrice = BigDecimal("140.00"))
      val mds = FakeMarketDataService.single("AAPL", BigDecimal("150.00"))
      val userRepo = new FakeUserRepository(Some(user))
      val holdingRepo = new FakeHoldingRepository(initialHolding = Some(holding))
      val txRepo = new FakeTransactionRepository

      val service = tradingServiceWith(mds, userRepo, holdingRepo, txRepo)
      service.sell(userId = 1L, symbol = "AAPL", quantity = 3).futureValue

      // Holding should be updated with reduced quantity but same averageBuyPrice
      holdingRepo.savedHoldings mustBe List(
        Holding(userId = 1L, symbol = "AAPL", quantity = 7, averageBuyPrice = BigDecimal("140.00"))
      )
      holdingRepo.deletedHoldings mustBe List.empty
    }

    "delete holding when all shares are sold" in {
      val user = User(id = 1L, username = "user1", passwordHash = "hash", cashBalance = BigDecimal("5000.00"))
      val holding = Holding(userId = 1L, symbol = "AAPL", quantity = 5, averageBuyPrice = BigDecimal("140.00"))
      val mds = FakeMarketDataService.single("AAPL", BigDecimal("150.00"))
      val userRepo = new FakeUserRepository(Some(user))
      val holdingRepo = new FakeHoldingRepository(initialHolding = Some(holding))
      val txRepo = new FakeTransactionRepository

      val service = tradingServiceWith(mds, userRepo, holdingRepo, txRepo)
      service.sell(userId = 1L, symbol = "AAPL", quantity = 5).futureValue

      // Holding should be deleted, not upserted
      holdingRepo.savedHoldings mustBe List.empty
      holdingRepo.deletedHoldings mustBe List((1L, "AAPL"))
    }

    "create SELL transaction in history" in {
      val user = User(id = 1L, username = "user1", passwordHash = "hash", cashBalance = BigDecimal("5000.00"))
      val holding = Holding(userId = 1L, symbol = "AAPL", quantity = 10, averageBuyPrice = BigDecimal("140.00"))
      val mds = FakeMarketDataService.single("AAPL", BigDecimal("150.00"))
      val userRepo = new FakeUserRepository(Some(user))
      val holdingRepo = new FakeHoldingRepository(initialHolding = Some(holding))
      val txRepo = new FakeTransactionRepository

      val service = tradingServiceWith(mds, userRepo, holdingRepo, txRepo)
      service.sell(userId = 1L, symbol = "AAPL", quantity = 2).futureValue

      txRepo.savedTransactions.size mustBe 1
      val savedTx = txRepo.savedTransactions.head
      savedTx.side mustBe TransactionSide.Sell
      savedTx.symbol mustBe "AAPL"
      savedTx.quantity mustBe 2
      savedTx.price mustBe BigDecimal("150.00")
    }
  }

  // ===========================================
  // PortfolioService tests
  // ===========================================

  "PortfolioService.getPortfolio" should {

    "return UserNotFound when user does not exist" in {
      val mds = FakeMarketDataService(Map.empty)
      val userRepo = new FakeUserRepository(None)
      val holdingRepo = new FakeHoldingRepository()

      val service = portfolioServiceWith(mds, userRepo, holdingRepo)
      val result = service.getPortfolio(userId = 1L).futureValue

      result mustBe Left(TradingError.UserNotFound)
    }

    "return empty positions when user has no holdings" in {
      val user = User(id = 1L, username = "user1", passwordHash = "hash", cashBalance = BigDecimal("100000.00"))
      val mds = FakeMarketDataService(Map.empty)
      val userRepo = new FakeUserRepository(Some(user))
      val holdingRepo = new FakeHoldingRepository()

      val service = portfolioServiceWith(mds, userRepo, holdingRepo)
      val result = service.getPortfolio(userId = 1L).futureValue

      val portfolio = result.value
      portfolio.userId mustBe 1L
      portfolio.cashBalance mustBe BigDecimal("100000.00")
      portfolio.positions mustBe List.empty
      portfolio.totalStockValue mustBe BigDecimal(0)
      portfolio.totalAccountValue mustBe BigDecimal("100000.00")
    }

    "calculate currentValue for each position" in {
      val user = User(id = 1L, username = "user1", passwordHash = "hash", cashBalance = BigDecimal("50000.00"))
      val holdings = List(
        Holding(userId = 1L, symbol = "AAPL", quantity = 10, averageBuyPrice = BigDecimal("140.00")),
        Holding(userId = 1L, symbol = "MSFT", quantity = 5, averageBuyPrice = BigDecimal("400.00"))
      )
      val mds = FakeMarketDataService(Map("AAPL" -> BigDecimal("150.00"), "MSFT" -> BigDecimal("420.00")))
      val userRepo = new FakeUserRepository(Some(user))
      val holdingRepo = new FakeHoldingRepository(initialHoldings = holdings)

      val service = portfolioServiceWith(mds, userRepo, holdingRepo)
      val result = service.getPortfolio(userId = 1L).futureValue

      val portfolio = result.value
      val aaplPosition = portfolio.positions.find(_.symbol == "AAPL").get
      val msftPosition = portfolio.positions.find(_.symbol == "MSFT").get

      // AAPL: 150.00 * 10 = 1500.00
      aaplPosition.currentValue mustBe BigDecimal("1500.00")
      // MSFT: 420.00 * 5 = 2100.00
      msftPosition.currentValue mustBe BigDecimal("2100.00")
    }

    "calculate profitLoss for each position" in {
      val user = User(id = 1L, username = "user1", passwordHash = "hash", cashBalance = BigDecimal("50000.00"))
      val holdings = List(
        Holding(userId = 1L, symbol = "AAPL", quantity = 10, averageBuyPrice = BigDecimal("140.00")),
        Holding(userId = 1L, symbol = "MSFT", quantity = 5, averageBuyPrice = BigDecimal("400.00"))
      )
      val mds = FakeMarketDataService(Map("AAPL" -> BigDecimal("150.00"), "MSFT" -> BigDecimal("380.00")))
      val userRepo = new FakeUserRepository(Some(user))
      val holdingRepo = new FakeHoldingRepository(initialHoldings = holdings)

      val service = portfolioServiceWith(mds, userRepo, holdingRepo)
      val result = service.getPortfolio(userId = 1L).futureValue

      val portfolio = result.value
      val aaplPosition = portfolio.positions.find(_.symbol == "AAPL").get
      val msftPosition = portfolio.positions.find(_.symbol == "MSFT").get

      // AAPL profit: (150 * 10) - (140 * 10) = 1500 - 1400 = 100
      aaplPosition.profitLoss mustBe BigDecimal("100.00")
      // MSFT loss: (380 * 5) - (400 * 5) = 1900 - 2000 = -100
      msftPosition.profitLoss mustBe BigDecimal("-100.00")
    }

    "calculate totalStockValue and totalAccountValue" in {
      val user = User(id = 1L, username = "user1", passwordHash = "hash", cashBalance = BigDecimal("50000.00"))
      val holdings = List(
        Holding(userId = 1L, symbol = "AAPL", quantity = 10, averageBuyPrice = BigDecimal("140.00")),
        Holding(userId = 1L, symbol = "MSFT", quantity = 5, averageBuyPrice = BigDecimal("400.00"))
      )
      val mds = FakeMarketDataService(Map("AAPL" -> BigDecimal("150.00"), "MSFT" -> BigDecimal("420.00")))
      val userRepo = new FakeUserRepository(Some(user))
      val holdingRepo = new FakeHoldingRepository(initialHoldings = holdings)

      val service = portfolioServiceWith(mds, userRepo, holdingRepo)
      val result = service.getPortfolio(userId = 1L).futureValue

      val portfolio = result.value
      // totalStockValue = 1500.00 + 2100.00 = 3600.00
      portfolio.totalStockValue mustBe BigDecimal("3600.00")
      // totalAccountValue = 50000.00 + 3600.00 = 53600.00
      portfolio.totalAccountValue mustBe BigDecimal("53600.00")
    }
  }
}
