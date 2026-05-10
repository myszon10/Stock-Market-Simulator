package controllers

import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.BaseIntegrationSpec

class AuthControllerSpec extends BaseIntegrationSpec {

  "AuthController" should {

    "successfully register a new user and return 201 Created" in {
      val requestBody = Json.obj(
        "username" -> "demo_register",
        "password" -> "password123"
      )

      val request = FakeRequest(POST, "/api/auth/register").withJsonBody(requestBody)
      val result = route(app, request).get

      status(result) mustBe CREATED
      val json = contentAsJson(result)
      (json \ "username").as[String] mustBe "demo_register"
      (json \ "cashBalance").as[BigDecimal] mustBe BigDecimal("100000.00")
    }

    "return 409 Conflict when trying to register an existing username" in {
      val requestBody = Json.obj(
        "username" -> "conflict_user",
        "password" -> "password123"
      )

      val firstRequest = FakeRequest(POST, "/api/auth/register").withJsonBody(requestBody)
      status(route(app, firstRequest).get) mustBe CREATED

      val secondRequest = FakeRequest(POST, "/api/auth/register").withJsonBody(requestBody)
      val result = route(app, secondRequest).get

      status(result) mustBe CONFLICT
      (contentAsJson(result) \ "error").as[String] mustBe "USER_ALREADY_EXISTS"
    }

    "return 400 Bad Request for an invalid registration JSON body" in {
      val requestBody = Json.obj("username" -> "bad_request_user")
      val request = FakeRequest(POST, "/api/auth/register").withJsonBody(requestBody)
      val result = route(app, request).get

      status(result) mustBe BAD_REQUEST
      (contentAsJson(result) \ "error").as[String] mustBe "BAD_REQUEST"
    }

    "successfully login an existing user and set a session cookie" in {
      val requestBody = Json.obj(
        "username" -> "demo_login",
        "password" -> "password123"
      )
      val registerReq = FakeRequest(POST, "/api/auth/register").withJsonBody(requestBody)
      status(route(app, registerReq).get) mustBe CREATED

      val loginReq = FakeRequest(POST, "/api/auth/login").withJsonBody(requestBody)
      val result = route(app, loginReq).get

      status(result) mustBe OK
      session(result).get("userId") mustBe defined
      session(result).get("username").value mustBe "demo_login"
    }

    "return 401 Unauthorized for an incorrect password" in {
      val registerBody = Json.obj("username" -> "wrong_pass_user", "password" -> "correct_password")
      status(route(app, FakeRequest(POST, "/api/auth/register").withJsonBody(registerBody)).get) mustBe CREATED

      val loginBody = Json.obj("username" -> "wrong_pass_user", "password" -> "WRONG_password")
      val loginReq = FakeRequest(POST, "/api/auth/login").withJsonBody(loginBody)
      val result = route(app, loginReq).get

      status(result) mustBe UNAUTHORIZED
      (contentAsJson(result) \ "error").as[String] mustBe "UNAUTHORIZED"
    }

    "return 401 Unauthorized for a non-existent user" in {
      val loginBody = Json.obj("username" -> "non_existent_user", "password" -> "any_password")
      val loginReq = FakeRequest(POST, "/api/auth/login").withJsonBody(loginBody)
      val result = route(app, loginReq).get

      status(result) mustBe UNAUTHORIZED
      (contentAsJson(result) \ "error").as[String] mustBe "UNAUTHORIZED"
    }

    "successfully logout and clear the session cookie" in {
      val request = FakeRequest(POST, "/api/auth/logout").withSession("userId" -> "1")
      val result = route(app, request).get

      status(result) mustBe OK
      session(result).get("userId") mustBe empty
      (contentAsJson(result) \ "message").as[String] mustBe "Logged out"
    }
  }
}
