package models.dto.auth

import play.api.libs.json.{Json, OFormat}

case class RegisterRequest(username: String, password: String)

object RegisterRequest {
  implicit val format: OFormat[RegisterRequest] = Json.format[RegisterRequest]
}

case class LoginRequest(username: String, password: String)

object LoginRequest {
  implicit val format: OFormat[LoginRequest] = Json.format[LoginRequest]
}
