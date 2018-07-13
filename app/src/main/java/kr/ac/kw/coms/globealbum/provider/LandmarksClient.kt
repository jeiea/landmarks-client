package kr.ac.kw.coms.globealbum.provider

//import com.google.gson.JsonObject
import awaitStringResult
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.beust.klaxon.json
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import okhttp3.FormBody
import okhttp3.Request

class LandmarksClient {

  private val authUrl = "https://coms-globe.herokuapp.com/auth/page/email"
  val gson = Gson()

  suspend fun register(email: String): String {
    val obj = json {
      obj("email" to email)
    }
    return Fuel.post(authUrl + "/register")
      .header("Accept" to "application/json")
      .header("Content-Type" to "application/json")
      .header("User-Agent" to "landmarks-client")
      .body(obj.toString())
      .awaitStringResult()
  }

  fun login(url: String, id: String, password: String) {
    val requestBody = FormBody.Builder().add("userId", id).add("userPassword", password).build()

    val request = Request.Builder().url(url).post(requestBody).build()
  }

  val chromeAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.59 Safari/537.36"
  suspend fun reverseGeocode(latitude: Double, longitude: Double): Pair<String?, String?>? {
    val params = listOf(
      "format" to "json",
      "lat" to latitude,
      "lon" to longitude)
    val json: String = Fuel.get("https://nominatim.openstreetmap.org/reverse", params)
      .header("User-Agent" to chromeAgent)
      .awaitStringResult()
    val res = Parser().parse(StringBuilder(json)) as JsonObject
    val addr = res.obj("address") ?: return null
    return addr.string("country") to addr.string("city")
  }

  fun<T> reverseGeoJava(latitude: Double, longitude: Double): Deferred<Pair<String?, String?>?> {
    return async(CommonPool) {
      reverseGeocode(latitude, longitude)
    }
  }
//
//  fun<T> getCont(): Continuation<T> {
//    return runBlocking {
//      suspendCoroutine<Continuation<T>> { lock ->
//        async(CommonPool) {
//          suspendCoroutine<T> { lock.resume(it) }
//        }
//      }
//    }
//  }
}