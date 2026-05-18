package models

case class Quote(
  symbol: String,
  price: BigDecimal,
  fetchedAt: java.time.Instant
)
