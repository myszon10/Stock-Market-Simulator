package models.dto.leaderboard

import play.api.libs.json.{Json, OFormat}

case class LeaderboardEntry(
                           rank: Int,
                           username: String,
                           totalAccountValue: BigDecimal,
                           profitLoss: BigDecimal
                           )

object LeaderboardEntry {
  implicit val format: OFormat[LeaderboardEntry] = Json.format[LeaderboardEntry]
}