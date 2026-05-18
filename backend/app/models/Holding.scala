package models

case class Holding(
  userId: Long,
  symbol: String,
  quantity: Int,
  averageBuyPrice: BigDecimal
)
