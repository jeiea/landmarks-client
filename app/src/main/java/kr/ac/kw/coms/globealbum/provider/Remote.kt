package kr.ac.kw.coms.globealbum.provider

import android.util.Log
import com.beust.klaxon.JsonBase
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.beust.klaxon.internal.firstNotNullResult
import com.beust.klaxon.json
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.cookies.AcceptAllCookiesStorage
import io.ktor.client.features.cookies.HttpCookies
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.io.jvm.javaio.toOutputStream
import kr.ac.kw.coms.globealbum.common.MultiPartContent
import kr.ac.kw.coms.globealbum.common.copyToSuspend
import java.io.File
import java.util.*
import kotlin.coroutines.experimental.coroutineContext
import kotlin.math.max

fun HttpRequestBuilder.userAgent() {
  header("User-Agent", "landmarks-client")
}

fun HttpRequestBuilder.json(json: JsonBase) {
  contentType(ContentType.Application.Json)
  body = json.toJsonString()
}

val herokuUri = "https://landmarks-coms.herokuapp.com"

open class Promise<T> {
  var ans: T? = null
  var err: Throwable? = null

  open fun success(result: T) {
    ans = result
  }

  open fun failure(cause: Throwable) {
    err = cause
  }

  open fun resolve(result: T) = success(result)
  open fun resolve(cause: Throwable) = failure(cause)
  open fun resolve(block: suspend CoroutineScope.() -> T) {
    resolve(runBlocking(block = block))
  }
}

open class UIPromise<T> : Promise<T>() {
  override fun resolve(block: suspend CoroutineScope.() -> T) {
    runBlocking {
      launch(UI) {
        try {
          resolve(async(CommonPool, block = block).await())
        } catch (e: Throwable) {
          resolve(e)
        }
      }
    }
  }
}

class RemoteJava {

  val client = Remote()

  fun reverseGeocode(latitude: Double, longitude: Double, prom: Promise<Pair<String?, String?>?>) {
    prom.resolve { client.reverseGeocode(latitude, longitude) }
  }

}

class Remote(val http: HttpClient, val basePath: String) {

  constructor() : this(HttpClient(Android.create()) {
    install(HttpCookies) {
      storage = AcceptAllCookiesStorage()
    }
  }, herokuUri)

  val lastHostReqTime = hashMapOf<String, Long>()
  val chromeAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.59 Safari/537.36"

  @Synchronized
  suspend fun reverseGeocode(latitude: Double, longitude: Double): Pair<String?, String?>? {
    val last: Long = lastHostReqTime["nominatim.openstreetmap.org"] ?: 0
    delay(max(0, last + 1000 - Date().time))
    val json: String = http.get("https://nominatim.openstreetmap.org/reverse") {
      parameter("format", "json")
      parameter("lat", latitude.toString())
      parameter("lon", longitude.toString())
      userAgent(chromeAgent)
    }
    val res = Parser().parse(StringBuilder(json)) as JsonObject
    val addr = res.obj("address") ?: return null
    val detail = listOf("city", "county", "town", "attraction").map(addr::string).firstNotNullResult { it }
    return addr.string("country") to detail
  }

  private fun throwIfFailure(resp: String) {
    if (!resp.contains("success")) {
      throw RuntimeException(resp)
    }
  }

  suspend fun checkAlive(): Boolean {
    val resp: String = http.get(basePath)
    return resp.contains("Hello")
  }

  suspend fun register(ident: String, pass: String, email: String, nick: String) {
    val regFields: JsonObject = json {
      obj(
        "login" to ident,
        "password" to pass,
        "email" to email,
        "nick" to nick
      )
    }
    val resp: String = http.post("$basePath/auth/register") {
      userAgent()
      json(regFields)
    }
    throwIfFailure(resp)
  }

  suspend fun login(ident: String, pass: String) {
    val param: JsonObject = json { obj("login" to ident, "password" to pass) }
    val resp: String = http.post("$basePath/auth/login") {
      userAgent()
      json(param)
    }
    throwIfFailure(resp)
  }

  suspend fun uploadPic(file: File, latitude: Float? = null, longitude: Float? = null, addr: String? = null) {
    val resp: String = http.request {
      method = HttpMethod.Put
      url.takeFrom("$basePath/picture")
      body = MultiPartContent.build {
        latitude?.also { add("lat", it.toString()) }
        longitude?.also { add("lon", it.toString()) }
        addr?.also { add("address", it) }
        add("pic0", filename = file.name) {
          file.inputStream().copyToSuspend(toOutputStream())
        }
      }
    }
    throwIfFailure(resp)
  }
}
