package kr.ac.kw.coms.globealbum

import kotlinx.coroutines.experimental.runBlocking
import kr.ac.kw.coms.globealbum.provider.LandmarksClient
import org.amshove.kluent.`should be equal to`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.xit
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
    xit("can register") {
      val n = Random().nextInt(10000000).toString()
      runBlocking {
        LandmarksClient().register("${n}@grr.la")
      }
    }
    val lc = LandmarksClient()
    it("can do reverse geocoding") {
      runBlocking {
        val s = lc.reverseGeocode(37.54567, 126.9944)
        s!!.first!! `should be equal to`  "대한민국"
        s.second!! `should be equal to`  "서울특별시"
      }
    }
  }
})
