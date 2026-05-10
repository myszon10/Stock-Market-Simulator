package repositories

import models.Transaction

import scala.concurrent.{ExecutionContext, Future}

class TransactionRepository(implicit ec: ExecutionContext) {
  def create(transaction: Transaction): Future[Transaction] =
    Future.successful(transaction)
}