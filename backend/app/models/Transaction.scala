package models

enum TransactionSide:
  case Buy, Sell

case class Transaction(
  id: Long,
  userId: Long,
  symbol: String,
  side: TransactionSide,
  quantity: Int,
  price: BigDecimal,
  createdAt: java.time.Instant
)
