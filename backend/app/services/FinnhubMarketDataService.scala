package services

import models.Quote
import models.errors.MarketDataError
import play.api.libs.json.Json

import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Locale
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.blocking
import scala.sys.process.ProcessLogger
import scala.sys.process.*
import scala.util.Try


object FinnhubMarketDataService:
    type HttpGet = (URI, String) => Future[Either[Throwable, (Int, String)]]

    private val requestTimeoutSeconds = 5

    private val curlExecutable =
        if System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win") then
            "curl.exe"
        else
            "curl"

    val defaultHttpGet: HttpGet = (uri: URI, apiKey: String) =>
      Future {
          blocking {
              val stdout = new StringBuilder
              val stderr = new StringBuilder

              val command = Seq(
                  curlExecutable,
                  "--silent",
                  "--show-error",
                  "--location",
                  "--max-time", requestTimeoutSeconds.toString,
                  "--write-out", "\n%{http_code}",
                  uri.toString,
                  "--header", "Accept: application/json",
                  "--header", "User-Agent: curl/8.0.0",
                  "--header", s"X-Finnhub-Token: $apiKey"
              )

              val exitCode = command.!(
                  ProcessLogger(
                      line => stdout.append(line).append('\n'),
                      line => stderr.append(line).append('\n')
                  )
              )

              if exitCode != 0 then
                  Left(
                      RuntimeException(
                          s"curl failed with exit code $exitCode: ${stderr.toString.take(200)}"
                      )
                  )
              else
                  parseCurlOutput(stdout.toString)
          }
      }(using ExecutionContext.global)

    private def parseCurlOutput(output: String): Either[Throwable, (Int, String)] =
      val trimmedOutput = output.stripTrailing()
      val statusSeparatorIndex = trimmedOutput.lastIndexOf('\n')

      if statusSeparatorIndex < 0 then
          Left(RuntimeException("curl response did not contain HTTP status code"))
      else
          val responseBody = trimmedOutput.substring(0, statusSeparatorIndex)
          val statusText = trimmedOutput.substring(statusSeparatorIndex + 1).trim

          Try(statusText.toInt).toEither match {
              case Right(statusCode) =>
                  Right(statusCode -> responseBody)

              case Left(exception) =>
                  Left(exception)
          }

class FinnhubMarketDataService(
                                apiKey: Option[String],
                                httpGet: FinnhubMarketDataService.HttpGet = FinnhubMarketDataService.defaultHttpGet
                              )(using ec: ExecutionContext) extends MarketDataService:

    private val quoteEndpoint = "https://finnhub.io/api/v1/quote"

    override def getQuote(symbol: String): Future[Either[MarketDataError, Quote]] =
        val normalizedSymbol = normalizeSymbol(symbol)

        if !StockCatalog.isSupported(normalizedSymbol) then
            Future.successful(
                Left(MarketDataError.UnsupportedSymbol(normalizedSymbol))
            )
        else
            apiKey match {
                case None =>
                    Future.successful(
                        Left(MarketDataError.MissingApiKey)
                    )

                case Some(key) =>
                    fetchQuote(normalizedSymbol, key)
            }

    private def fetchQuote(symbol: String, key: String): Future[Either[MarketDataError, Quote]] =
        val uri = buildQuoteUri(symbol)

        httpGet(uri, key).map {
            case Left(exception) =>
                println(s"[Finnhub] HTTP request failed: ${exception.getClass.getName}: ${exception.getMessage}")
                Left(MarketDataError.ExternalServiceUnavailable)

            case Right((statusCode, responseBody)) =>
                if isSuccessfulStatus(statusCode) then
                    parseQuote(symbol, responseBody)
                else
                    Left(MarketDataError.ExternalServiceUnavailable)
        }

    private def parseQuote(symbol: String, responseBody: String): Either[MarketDataError, Quote] =
        Try(Json.parse(responseBody)).toEither match {
            case Left(_) =>
                Left(MarketDataError.ExternalServiceUnavailable)

            case Right(json) =>
                val currentPrice = (json \ "c").asOpt[BigDecimal]

                currentPrice match {
                    case Some(price) if price > 0 =>
                        Right(
                            Quote(
                                symbol = symbol,
                                price = price,
                                fetchedAt = Instant.now()
                            )
                        )

                    case _ =>
                        Left(MarketDataError.QuoteNotAvailable(symbol))
                }
        }

    private def buildQuoteUri(symbol: String): URI =
        val encodedSymbol = urlEncode(symbol)

        URI.create(s"$quoteEndpoint?symbol=$encodedSymbol")

    private def urlEncode(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8)

    private def isSuccessfulStatus(statusCode: Int): Boolean =
        statusCode >= 200 && statusCode < 300

    private def normalizeSymbol(str: String): String =
        str.trim.toUpperCase(Locale.ROOT)