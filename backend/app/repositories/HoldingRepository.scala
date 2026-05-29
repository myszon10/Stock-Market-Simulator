package repositories

import anorm.*
import anorm.SqlParser.*
import models.Holding
import play.api.db.Database

import javax.inject.Inject
import javax.inject.Singleton
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class HoldingRepository @Inject()(db: Database)(implicit ec: ExecutionContext) {

  private val holdingParser: RowParser[Holding] = (
    get[Long]("user_id") ~
      get[String]("symbol") ~
      get[Int]("quantity") ~
      get[BigDecimal]("average_buy_price")
    ) map {
    case userId ~ symbol ~ quantity ~ averageBuyPrice =>
      Holding(userId, symbol, quantity, averageBuyPrice)
  }

  def findByUserAndSymbol(userId: Long, symbol: String): Future[Option[Holding]] = Future {
    db.withConnection { implicit connection =>
      SQL"""
        SELECT user_id, symbol, quantity, average_buy_price
        FROM holdings
        WHERE user_id = $userId AND symbol = $symbol
      """.as(holdingParser.singleOpt)
    }
  }

  def findByUserId(userId: Long): Future[List[Holding]] = Future {
    db.withConnection { implicit connection =>
      SQL"""
        SELECT user_id, symbol, quantity, average_buy_price
        FROM holdings
        WHERE user_id = $userId
        ORDER BY symbol
      """.as(holdingParser.*)
    }
  }

  def upsert(holding: Holding): Future[Unit] = Future {
    db.withConnection { implicit connection =>
      SQL"""
        INSERT INTO holdings (user_id, symbol, quantity, average_buy_price)
        VALUES (${holding.userId}, ${holding.symbol}, ${holding.quantity}, ${holding.averageBuyPrice})
        ON CONFLICT (user_id, symbol)
        DO UPDATE SET
          quantity = EXCLUDED.quantity,
          average_buy_price = EXCLUDED.average_buy_price
      """.executeUpdate()
    }
  }

  def delete(userId: Long, symbol: String): Future[Unit] = Future {
    db.withConnection { implicit connection =>
      SQL"""
        DELETE FROM holdings
        WHERE user_id = $userId AND symbol = $symbol
      """.executeUpdate()
    }
  }
}