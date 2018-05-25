package kr.ac.kw.coms.globealbum

import kr.ac.kw.coms.globealbum.provider.LandmarkClient
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun registerTest() {
      val res = LandmarkClient().register("holy@grr.la")
      println(res)
    }
}
