package repositories

import anorm._
import anorm.SqlParser._
import models.User
import org.postgresql.util.PSQLException
import play.api.db.Database
import scala.util.control.NonFatal

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserRepository @Inject()(db: Database)(implicit ec: ExecutionContext) {
  private val userParser: RowParser[User] = (
    get[Long]("id") ~
      get[String]("username") ~
      get[String]("password_hash") ~
      get[BigDecimal]("cash_balance")
    ) map {
    case id ~ username ~ passwordHash ~ cashBalance =>
      User(id, username, passwordHash, cashBalance)
  }

  def create(username: String, passwordHash: String, cashBalance: BigDecimal): Future[Option[User]] = {
    val createdUserFuture = Future {
      db.withConnection { implicit connection =>
        val id: Long =
          SQL"""
            INSERT INTO users (username, password_hash, cash_balance)
            VALUES ($username, $passwordHash, $cashBalance)
            RETURNING id
          """.as(scalar[Long].single)

        User(id, username, passwordHash, cashBalance)
      }
    }

    createdUserFuture.map(Some(_)).recover {
      // 23505 state indicates a unique constraint violation, which means the username already exists
      case e: PSQLException if e.getSQLState == "23505" =>
        None
      case NonFatal(e) =>
        throw e
    }
  }

  def findById(id: Long): Future[Option[User]] = Future {
    db.withConnection { implicit connection =>
      SQL"SELECT * FROM users WHERE id = $id".as(userParser.singleOpt)
    }
  }

  def findByUsername(username: String): Future[Option[User]] = Future {
    db.withConnection { implicit connection =>
      SQL"SELECT * FROM users WHERE username = $username".as(userParser.singleOpt)
    }
  }

  def existsByUsername(username: String): Future[Boolean] = Future {
    db.withConnection { implicit connection =>
      SQL"SELECT EXISTS(SELECT 1 FROM users WHERE username = $username)".as(scalar[Boolean].single)
    }
  }

  def updateCashBalance(userId: Long, newBalance: BigDecimal): Future[Int] = Future {
    db.withConnection { implicit connection =>
      SQL"""
        UPDATE users
        SET cash_balance = $newBalance
        WHERE id = $userId
      """.executeUpdate()
    }
  }
}
