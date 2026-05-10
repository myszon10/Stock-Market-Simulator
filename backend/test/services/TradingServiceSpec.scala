package services

import models.errors.TradingError
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.db.Database
import repositories.{HoldingRepository, TransactionRepository, UserRepository}

import java.lang.reflect.Proxy
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class TradingServiceSpec extends AnyWordSpec with Matchers with ScalaFutures {

  private class FakeMarketDataService extends MarketDataService {
    override def getQuote(symbol: String): Future[Either[models.errors.MarketDataError, models.Quote]] =
      Future.failed(new RuntimeException("MarketDataService should not be called in these tests"))
  }

  private val dummyDatabase: Database =
    Proxy.newProxyInstance(
      classOf[Database].getClassLoader,
      Array(classOf[Database]),
      (_, _, _) => throw new UnsupportedOperationException("Database should not be used in these tests")
    ).asInstanceOf[Database]

  private val marketDataService = new FakeMarketDataService
  private val userRepository = new UserRepository(dummyDatabase)(global)
  private val holdingRepository = new HoldingRepository()(global)
  private val transactionRepository = new TransactionRepository()(global)

  private val service = new TradingService(
    marketDataService,
    userRepository,
    holdingRepository,
    transactionRepository
  )(global)

  "TradingService.buy" should {
    "return InvalidQuantity when quantity <= 0" in {
      whenReady(service.buy(userId = 1L, symbol = "AAPL", quantity = 0)) { result =>
        result mustBe Left(TradingError.InvalidQuantity)
      }
    }

    "return domain error instead of throwing exception for invalid quantity" in {
      noException should be thrownBy {
        val result = service.buy(userId = 1L, symbol = "AAPL", quantity = -5).futureValue
        result mustBe Left(TradingError.InvalidQuantity)
      }
    }
  }
}