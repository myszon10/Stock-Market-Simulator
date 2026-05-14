package repositories

import anorm.*
import anorm.SqlParser.*
import models.Quote
import play.api.db.Database

import java.sql.Timestamp
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class PriceCacheRepository @Inject()(db: Database)(using ec: ExecutionContext):

    private val quoteParser: RowParser[Quote] =
        (
          get[String]("symbol") ~
            get[BigDecimal]("price") ~
            get[Date]("fetched_at")
          ).map {
            case symbol ~ price ~ fetchedAt =>
                Quote(
                    symbol = symbol,
                    price = price,
                    fetchedAt = fetchedAt.toInstant
                )
        }
        
    def findBySymbol(symbol: String): Future[Option[Quote]] =
        val normalizedSymbol = normalizeSymbol(symbol)
        
        Future {
            db.withConnection { implicit connection =>
                SQL"""
                     SELECT symbol, price, fetched_at
                     FROM price_cache
                     WHERE symbol = $normalizedSymbol
                """.as(quoteParser.singleOpt)                
            }
        }
        
    def upsert(quote: Quote): Future[Int] =
        val normalizedSymbol = normalizeSymbol(quote.symbol)
        val fetchedAt = Timestamp.from(quote.fetchedAt)
        
        Future {
            db.withConnection { implicit connection =>
              SQL"""
                    INSERT INTO price_cache (symbol, price, fetched_at)
                    VALUES ($normalizedSymbol, ${quote.price}, $fetchedAt)
                    ON CONFLICT (symbol)
                    DO UPDATE SET
                        price = EXCLUDED.price,
                        fetched_at = EXCLUDED.fetched_at
              """.executeUpdate()
            }
        }
        
    private def normalizeSymbol(symbol: String): String =
        symbol.trim.toUpperCase(Locale.ROOT)