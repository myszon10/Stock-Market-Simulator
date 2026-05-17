package repositories

import anorm._
import anorm.SqlParser._
import models.{Transaction, TransactionSide}
import play.api.db.Database

import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TransactionRepository @Inject()(db: Database)(implicit ec: ExecutionContext) {

  private val transactionParser: RowParser[Transaction] = (
    get[Long]("id") ~
      get[Long]("user_id") ~
      get[String]("symbol") ~
      get[String]("side") ~
      get[Int]("quantity") ~
      get[BigDecimal]("price") ~
      get[Instant]("created_at")
    ) map {
    case id ~ userId ~ symbol ~ side ~ quantity ~ price ~ createdAt =>
      val transactionSide = if (side.equalsIgnoreCase("BUY")) TransactionSide.Buy else TransactionSide.Sell
      Transaction(id, userId, symbol, transactionSide, quantity, price, createdAt)
  }

  def create(transaction: Transaction): Future[Transaction] = Future {
    db.withConnection { implicit connection =>
      val id: Long =
        SQL"""
          INSERT INTO transactions (user_id, symbol, side, quantity, price, created_at)
          VALUES (${transaction.userId}, ${transaction.symbol}, ${transaction.side.toString.toUpperCase}, ${transaction.quantity}, ${transaction.price}, ${transaction.createdAt})
          RETURNING id
        """.as(scalar[Long].single)

      transaction.copy(id = id)
    }
  }

  def findByUserId(userId: Long): Future[List[Transaction]] = Future {
    db.withConnection { implicit connection =>
      SQL"""
        SELECT id, user_id, symbol, side, quantity, price, created_at
        FROM transactions
        WHERE user_id = $userId
        ORDER BY created_at DESC
      """.as(transactionParser.*)
    }
  }
}