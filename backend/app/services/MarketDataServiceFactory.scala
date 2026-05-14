package services

import play.api.Configuration
import repositories.PriceCacheRepository

import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.concurrent.duration.FiniteDuration

@Singleton
class MarketDataServiceFactory @Inject() (
                                         configuration: Configuration,
                                         priceCacheRepository: PriceCacheRepository
                                         )(using ec: ExecutionContext):

    private lazy val marketDataService: MarketDataService = createMarketDataService()

    def get(): MarketDataService = marketDataService

    private def createMarketDataService(): MarketDataService =
        val mode = configuration
          .getOptional[String]("marketData.mode")
          .getOrElse("mock")
          .trim
          .toLowerCase(Locale.ROOT)

        mode match {
            case "mock" =>
                MockMarketDataService()

            case "finnhub" =>
                val apiKey = configuration
                  .getOptional[String]("finnhub.apiKey")
                  .map(_.trim)
                  .filter(_.nonEmpty)

                CachedMarketDataService(
                    delegate = FinnhubMarketDataService(apiKey),
                    priceCacheRepository = priceCacheRepository,
                    ttl = cacheTtl
                )

            case _ =>
                MockMarketDataService()
        }

    private def cacheTtl: FiniteDuration =
        configuration
          .getOptional[Int]("marketData.cacheTtlSeconds")
          .getOrElse(60)
          .seconds
