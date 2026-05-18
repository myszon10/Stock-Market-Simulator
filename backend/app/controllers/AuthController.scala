package controllers

import models.dto.auth.{LoginRequest, LoginResponse, RegisterRequest, RegisterResponse}
import models.errors.AuthError

import javax.inject.*
import play.api.*
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.*
import services.AuthService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthController @Inject()(
  val controllerComponents: ControllerComponents,
  authService: AuthService
)(implicit ec: ExecutionContext) extends BaseController {

  def register(): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[RegisterRequest].fold(
      _ => Future.successful(BadRequest(Json.obj("error" -> "BAD_REQUEST", "message" -> "Invalid data."))),
      req => authService.register(req.username, req.password).map {
        case Right(user) =>
          Created(Json.toJson(RegisterResponse(user.id, user.username, user.cashBalance)))

        case Left(AuthError.UserAlreadyExists) =>
          Conflict(Json.obj("error" -> "USER_ALREADY_EXISTS", "message" -> "User already exists."))

        case Left(_) =>
          InternalServerError(Json.obj("error" -> "INTERNAL_SERVER_ERROR", "message" -> "Unexpected error."))
      }
    )
  }

  def login(): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[LoginRequest].fold(
      _ => Future.successful(BadRequest(Json.obj("error" -> "BAD_REQUEST", "message" -> "Invalid data."))),
      req => authService.login(req.username, req.password).map {
        case Right(user) =>
          Ok(Json.toJson(LoginResponse(user.id, user.username, user.cashBalance)))
          .withSession("userId" -> user.id.toString, "username" -> user.username)

        case Left(AuthError.InvalidCredentials) =>
          Unauthorized(Json.obj("error" -> "UNAUTHORIZED", "message" -> "Invalid credentials."))

        case Left(_) =>
          InternalServerError(Json.obj("error" -> "INTERNAL_SERVER_ERROR", "message" -> "Unexpected error."))
      }
    )
  }

  def logout(): Action[AnyContent] = Action {
    Ok(Json.obj("message" -> "Logged out")).withNewSession
  }
}
