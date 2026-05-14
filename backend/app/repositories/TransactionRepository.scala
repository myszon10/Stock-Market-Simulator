package repositories

import models.Transaction

import javax.inject.Inject
import javax.inject.Singleton
import scala.concurrent.ExecutionContext
import scala.concurrent.Future


@Singleton
class TransactionRepository @Inject()(implicit ec: ExecutionContext) {
  def create(transaction: Transaction): Future[Transaction] =
    Future.successful(transaction)
}