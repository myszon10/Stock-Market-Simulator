package repositories

import models.Holding

import javax.inject.Inject
import javax.inject.Singleton
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class HoldingRepository @Inject()(implicit ec: ExecutionContext) {
  def findByUserAndSymbol(userId: Long, symbol: String): Future[Option[Holding]] =
    Future.successful(None)

  def findByUserId(userId: Long): Future[List[Holding]] =
    Future.successful(List.empty)

  def upsert(holding: Holding): Future[Unit] =
    Future.successful(())

  def delete(userId: Long, symbol: String): Future[Unit] =
    Future.successful(())
}