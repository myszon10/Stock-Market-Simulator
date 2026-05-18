package utils

import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.db.Database
import play.api.db.evolutions.Evolutions
import play.api.test.Injecting

trait BaseIntegrationSpec extends PlaySpec 
  with GuiceOneAppPerSuite 
  with Injecting 
  with BeforeAndAfterEach { this: Suite =>

  lazy val database: Database = inject[Database]

  override def beforeEach(): Unit = {
    super.beforeEach()
    Evolutions.cleanupEvolutions(database)
    Evolutions.applyEvolutions(database)
  }

  override def afterEach(): Unit = {
    Evolutions.cleanupEvolutions(database)
    super.afterEach()
  }
}
