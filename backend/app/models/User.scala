package models

case class User (
  id: Long,
  username: String,
  passwordHash: String,
  cashBalance: BigDecimal
)
