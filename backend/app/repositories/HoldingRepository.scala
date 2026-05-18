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

  def upsert(holding: Holding): Future[Unit] =
    Future.successful(())
}