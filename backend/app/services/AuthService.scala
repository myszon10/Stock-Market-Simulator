package services

import models.User
import models.errors.AuthError
import repositories.UserRepository
import utils.PasswordHasher

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthService @Inject()(userRepository: UserRepository)(using ec: ExecutionContext) {

  private val initialBalance = BigDecimal("100000.00")

  def register(username: String, password: String): Future[Either[AuthError, User]] = {
    val hashedPassword = PasswordHasher.hash(password)

    userRepository.create(username, hashedPassword, initialBalance).map {
      case Some(user) => Right(user)
      case None       => Left(AuthError.UserAlreadyExists)
    }
  }

  def login(username: String, password: String): Future[Either[AuthError, User]] = {
    userRepository.findByUsername(username).map {
      case Some(user) if PasswordHasher.verify(password, user.passwordHash) =>
        Right(user)

      case _ =>
        Left(AuthError.InvalidCredentials)
    }
  }
}
