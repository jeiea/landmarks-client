package kr.ac.kw.coms.landmarks.client

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.io.File


@RunWith(JUnitPlatform::class)
class UploadSpek : Spek({
  describe("making heroku server testable") {
    val client = Remote(getTestClient())

    blit("resets server and uploads sample problems") {
//      client.resetAllDatabase()
      for (i in 0..3) {
        client.uploadPic(File("../coord$i.jpg"), i.toFloat(), i.toFloat(), "address$i")
      }
    }
  }
})