package services

import models.User
import models.errors.AuthError
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import repositories.UserRepository
import utils.PasswordHasher

import scala.concurrent.{ExecutionContext, Future}

class AuthServiceSpec extends AsyncWordSpec with Matchers with MockitoSugar {

  implicit val ec: ExecutionContext = ExecutionContext.global

  "AuthService" should {

    "register a new user successfully" in {
      val mockRepo = mock[UserRepository]
      val service = new AuthService(mockRepo)

      val expectedUser = User(1L, "testUser", "hashed_pwd", BigDecimal("100000.00"))

      when(mockRepo.create(eqTo("testUser"), any[String], eqTo(BigDecimal("100000.00"))))
        .thenReturn(Future.successful(Some(expectedUser)))

      service.register("testUser", "password123").map { result =>
        result mustBe Right(expectedUser)
      }
    }

    "return UserAlreadyExists when repository cannot create user" in {
      val mockRepo = mock[UserRepository]
      val service = new AuthService(mockRepo)

      when(mockRepo.create(any[String], any[String], any[BigDecimal]))
        .thenReturn(Future.successful(None))

      service.register("testUser", "password123").map { result =>
        result mustBe Left(AuthError.UserAlreadyExists)
      }
    }

    "login successfully with correct credentials" in {
      val mockRepo = mock[UserRepository]
      val service = new AuthService(mockRepo)

      val password = "password123"
      val hashedPassword = PasswordHasher.hash(password)
      val dbUser = User(1L, "testUser", hashedPassword, BigDecimal("100000.00"))

      when(mockRepo.findByUsername("testUser"))
        .thenReturn(Future.successful(Some(dbUser)))

      service.login("testUser", password).map { result =>
        result mustBe Right(dbUser)
      }
    }

    "fail to login with incorrect password" in {
      val mockRepo = mock[UserRepository]
      val service = new AuthService(mockRepo)

      val dbUser = User(1L, "testUser", PasswordHasher.hash("correct_password"), BigDecimal("100000.00"))

      when(mockRepo.findByUsername("testUser"))
        .thenReturn(Future.successful(Some(dbUser)))

      service.login("testUser", "wrong_password").map { result =>
        result mustBe Left(AuthError.InvalidCredentials)
      }
    }

    "fail to login when user does not exist" in {
      val mockRepo = mock[UserRepository]
      val service = new AuthService(mockRepo)

      when(mockRepo.findByUsername("nonexistent"))
        .thenReturn(Future.successful(None))

      service.login("nonexistent", "password123").map { result =>
        result mustBe Left(AuthError.InvalidCredentials)
      }
    }
  }
}