package kr.ac.kw.coms.globealbum

import kotlinx.coroutines.experimental.runBlocking
import kr.ac.kw.coms.globealbum.provider.LandmarksClient
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(JUnitPlatform::class)
class LandmarksClientSpek : Spek({
  describe("landmarks client") {
    it("can register") {
      val n = Random().nextInt(10000000).toString()
      runBlocking {
        LandmarksClient().register("${n}@grr.la")
      }
    }
  }
})
