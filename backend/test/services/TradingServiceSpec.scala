package services

import models.Holding
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

class TradingServiceSpec extends AnyWordSpec with Matchers with ScalaFutures with EitherValues {

  private val now: Instant = Instant.parse("2026-05-14T12:00:00Z")

  private class FakeMarketDataService(result: Either[MarketDataError, Quote]) extends MarketDataService {
    var requestedSymbols: List[String] = List.empty

    override def getQuote(symbol: String): Future[Either[MarketDataError, Quote]] = {
      requestedSymbols = requestedSymbols :+ symbol
      Future.successful(result)
    }
  }

  private class FakeUserRepository(initialUser: Option[User]) extends UserRepository(null)(global) {
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

  private class FakeHoldingRepository(initialHolding: Option[Holding]) extends HoldingRepository()(global) {
    var requestedHoldings: List[(Long, String)] = List.empty
    var savedHoldings: List[Holding] = List.empty

    override def findByUserAndSymbol(userId: Long, symbol: String): Future[Option[Holding]] = {
      requestedHoldings = requestedHoldings :+ (userId, symbol)
      Future.successful(initialHolding)
    }

    override def upsert(holding: Holding): Future[Unit] = {
      savedHoldings = savedHoldings :+ holding
      Future.successful(())
    }
  }

  private class FakeTransactionRepository extends TransactionRepository()(global) {
    var savedTransactions: List[Transaction] = List.empty

    override def create(transaction: Transaction): Future[Transaction] = {
      val savedTransaction = transaction.copy(id = 1001L)
      savedTransactions = savedTransactions :+ savedTransaction
      Future.successful(savedTransaction)
    }
  }

  private def serviceWith(
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
    )(global)

  "TradingService.buy" should {
    "return InvalidQuantity when quantity <= 0 without requesting price" in {
      val quote = Quote(
        symbol = "AAPL",
        price = BigDecimal("100.00"),
        fetchedAt = now
      )

      val marketDataService = new FakeMarketDataService(Right(quote))
      val userRepository = new FakeUserRepository(None)
      val holdingRepository = new FakeHoldingRepository(None)
      val transactionRepository = new FakeTransactionRepository

      val service = serviceWith(
        marketDataService,
        userRepository,
        holdingRepository,
        transactionRepository
      )

      val result = service.buy(userId = 1L, symbol = "AAPL", quantity = 0).futureValue

      result mustBe Left(TradingError.InvalidQuantity)
      marketDataService.requestedSymbols mustBe List.empty
      transactionRepository.savedTransactions mustBe List.empty
    }

    "buy stock using price from MarketDataService" in {
      val quote = Quote(
        symbol = "AAPL",
        price = BigDecimal("100.00"),
        fetchedAt = now
      )

      val user = User(
        id = 1L,
        username = "user1",
        passwordHash = "hash",
        cashBalance = BigDecimal("1000.00")
      )

      val marketDataService = new FakeMarketDataService(Right(quote))
      val userRepository = new FakeUserRepository(Some(user))
      val holdingRepository = new FakeHoldingRepository(None)
      val transactionRepository = new FakeTransactionRepository

      val service = serviceWith(
        marketDataService,
        userRepository,
        holdingRepository,
        transactionRepository
      )

      val result = service.buy(userId = 1L, symbol = " aapl ", quantity = 2).futureValue

      val transaction = result.value

      transaction.id mustBe 1001L
      transaction.userId mustBe 1L
      transaction.symbol mustBe "AAPL"
      transaction.side mustBe TransactionSide.Buy
      transaction.quantity mustBe 2
      transaction.price mustBe BigDecimal("100.00")

      marketDataService.requestedSymbols mustBe List("AAPL")
      userRepository.requestedUserIds mustBe List(1L)
      userRepository.updatedBalances mustBe List((1L, BigDecimal("800.00")))

      holdingRepository.requestedHoldings mustBe List((1L, "AAPL"))
      holdingRepository.savedHoldings mustBe List(
        Holding(
          userId = 1L,
          symbol = "AAPL",
          quantity = 2,
          averageBuyPrice = BigDecimal("100.00")
        )
      )

      transactionRepository.savedTransactions.map(_.price) mustBe List(BigDecimal("100.00"))
    }

    "not save transaction when price is unavailable" in {
      val marketDataService = new FakeMarketDataService(
        Left(MarketDataError.ExternalServiceUnavailable)
      )

      val userRepository = new FakeUserRepository(
        Some(
          User(
            id = 1L,
            username = "user1",
            passwordHash = "hash",
            cashBalance = BigDecimal("1000.00")
          )
        )
      )

      val holdingRepository = new FakeHoldingRepository(None)
      val transactionRepository = new FakeTransactionRepository

      val service = serviceWith(
        marketDataService,
        userRepository,
        holdingRepository,
        transactionRepository
      )

      val result = service.buy(userId = 1L, symbol = "AAPL", quantity = 2).futureValue

      result mustBe Left(TradingError.PriceUnavailable("AAPL"))
      marketDataService.requestedSymbols mustBe List("AAPL")
      userRepository.requestedUserIds mustBe List.empty
      userRepository.updatedBalances mustBe List.empty
      holdingRepository.savedHoldings mustBe List.empty
      transactionRepository.savedTransactions mustBe List.empty
    }

    "return UnsupportedSymbol when market data rejects symbol" in {
      val marketDataService = new FakeMarketDataService(
        Left(MarketDataError.UnsupportedSymbol("XYZ"))
      )

      val userRepository = new FakeUserRepository(None)
      val holdingRepository = new FakeHoldingRepository(None)
      val transactionRepository = new FakeTransactionRepository

      val service = serviceWith(
        marketDataService,
        userRepository,
        holdingRepository,
        transactionRepository
      )

      val result = service.buy(userId = 1L, symbol = "XYZ", quantity = 1).futureValue

      result mustBe Left(TradingError.UnsupportedSymbol("XYZ"))
      marketDataService.requestedSymbols mustBe List("XYZ")
      transactionRepository.savedTransactions mustBe List.empty
    }
  }
}