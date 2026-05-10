package repositories

import models.Holding

import scala.concurrent.{ExecutionContext, Future}

class HoldingRepository(implicit ec: ExecutionContext) {
  def findByUserAndSymbol(userId: Long, symbol: String): Future[Option[Holding]] =
    Future.successful(None)

  def upsert(holding: Holding): Future[Unit] =
    Future.successful(())
}