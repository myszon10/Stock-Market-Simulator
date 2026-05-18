package controllers.actions

import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserRequest[A](val userId: Long, val username: String, request: Request[A]) extends WrappedRequest[A](request)

class AuthenticatedAction @Inject()(parsers: PlayBodyParsers)(implicit val executionContext: ExecutionContext)
  extends ActionBuilder[UserRequest, AnyContent] with ActionRefiner[Request, UserRequest] {

  override val parser: BodyParser[AnyContent] = parsers.default

  override protected def refine[A](request: Request[A]): Future[Either[Result, UserRequest[A]]] = {
    val userIdOpt = request.session.get("userId").flatMap(_.toLongOption)
    val usernameOpt = request.session.get("username")

    (userIdOpt, usernameOpt) match {
      case (Some(id), Some(username)) =>
        Future.successful(Right(new UserRequest(id, username, request)))
      case _ =>
        Future.successful(Left(Unauthorized(Json.obj("error" -> "UNAUTHORIZED", "message" -> "You must be logged in to access this resource."))))
    }
  }
}