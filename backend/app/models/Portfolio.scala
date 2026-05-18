package models

case class PortfolioPosition(
  symbol: String,
  quantity: Int,
  averageBuyPrice: BigDecimal,
  currentPrice: BigDecimal,
  currentValue: BigDecimal,
  profitLoss: BigDecimal
)

case class Portfolio(
  userId: Long,
  cashBalance: BigDecimal,
  positions: List[PortfolioPosition],
  totalStockValue: BigDecimal,
  totalAccountValue: BigDecimal
)
