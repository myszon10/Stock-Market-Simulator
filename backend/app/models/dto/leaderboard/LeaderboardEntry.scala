package models.dto.leaderboard

import play.api.libs.json.{Json, OFormat}

case class LeaderboardPosition(
                                symbol: String,
                                quantity: Int,
                                currentPrice: BigDecimal,
                                currentValue: BigDecimal,
                                profitLoss: BigDecimal
                              )

object LeaderboardPosition {
  implicit val format: OFormat[LeaderboardPosition] = Json.format[LeaderboardPosition]
}

case class LeaderboardEntry(
                             rank: Int,
                             username: String,
                             totalAccountValue: BigDecimal,
                             profitLoss: BigDecimal,
                             positions: List[LeaderboardPosition]
                           )

object LeaderboardEntry {
  implicit val format: OFormat[LeaderboardEntry] = Json.format[LeaderboardEntry]
}