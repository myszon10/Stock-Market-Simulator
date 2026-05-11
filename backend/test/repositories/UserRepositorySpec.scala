package repositories

import utils.BaseIntegrationSpec

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class UserRepositorySpec extends BaseIntegrationSpec {

  lazy val repo: UserRepository = inject[UserRepository]

  val testUsername = "test_user"
  val testPasswordHash = "hashed_password_123"
  val initialBalance: BigDecimal = BigDecimal("100000.00")

  "UserRepository" should {

    "create a new user and retrieve it by ID" in {
      val createdUserOpt = Await.result(repo.create(testUsername, testPasswordHash, initialBalance), 5.seconds)

      createdUserOpt.mustBe(defined)
      val createdUser = createdUserOpt.get
      createdUser.id.must(be > 0L)
      createdUser.username.mustBe(testUsername)

      val foundUserOpt = Await.result(repo.findById(createdUser.id), 5.seconds)

      foundUserOpt.mustBe(defined)
      foundUserOpt.get.mustBe(createdUser)
    }

    "find a user by their existing username" in {
      val createdUserOpt = Await.result(repo.create(testUsername, testPasswordHash, initialBalance), 5.seconds)
      val foundUserOpt = Await.result(repo.findByUsername(testUsername), 5.seconds)

      createdUserOpt.mustBe(defined)
      val createdUser = createdUserOpt.get
      foundUserOpt.mustBe(defined)
      foundUserOpt.get.id.mustBe(createdUser.id)
    }

    "return None for a non-existent username" in {
      val foundUserOpt = Await.result(repo.findByUsername("non_existent_user"), 5.seconds)
      foundUserOpt.mustBe(empty)
    }

    "correctly update a user's cash balance" in {
      val createdUserOpt = Await.result(repo.create(testUsername, testPasswordHash, initialBalance), 5.seconds)
      val newBalance = BigDecimal("95000.50")

      createdUserOpt.mustBe(defined)
      val createdUser = createdUserOpt.get
      val affectedRows = Await.result(repo.updateCashBalance(createdUser.id, newBalance), 5.seconds)
      affectedRows.mustBe(1)

      val updatedUser = Await.result(repo.findById(createdUser.id), 5.seconds).get
      updatedUser.cashBalance.mustBe(newBalance)
    }

    "return 0 affected rows when updating a non-existent user" in {
      val nonExistentUserId = 9999L
      val affectedRows = Await.result(repo.updateCashBalance(nonExistentUserId, BigDecimal("0.00")), 5.seconds)
      affectedRows.mustBe(0)
    }

    "return true when checking for an existing username" in {
      Await.result(repo.create(testUsername, testPasswordHash, initialBalance), 5.seconds)
      val exists = Await.result(repo.existsByUsername(testUsername), 5.seconds)
      exists.mustBe(true)
    }

    "return false when checking for a non-existent username" in {
      val exists = Await.result(repo.existsByUsername("another_user"), 5.seconds)
      exists.mustBe(false)
    }
  }
}
