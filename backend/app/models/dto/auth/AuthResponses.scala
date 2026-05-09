package models.dto.auth

import play.api.libs.json.{Json, OFormat}

case class RegisterResponse(id: Long, username: String, cashBalance: BigDecimal)

object RegisterResponse {
  implicit val format: OFormat[RegisterResponse] = Json.format[RegisterResponse]
}

case class LoginResponse(userId: Long, username: String)

object LoginResponse {
  implicit val format: OFormat[LoginResponse] = Json.format[LoginResponse]
}
