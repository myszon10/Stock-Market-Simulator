package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test._
import play.api.test.Helpers._

class AuthControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {
  
  "AuthController logout" should {
    "return 200 OK with 'Logged out' message" in {
      val controller = new AuthController(stubControllerComponents())
      val result = controller.logout().apply(FakeRequest(GET, "/logout"))
      
      status(result) mustBe OK
      contentAsString(result) mustBe "Logged out"
    }
  }
}
