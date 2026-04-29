package controllers

import javax.inject._
import play.api._
import play.api.mvc._

@Singleton
class AuthController @Inject()(val controllerComponents: ControllerComponents)
extends BaseController {
  def logout(): Action[AnyContent] = Action {
    Ok("Logged out")
  }
}
