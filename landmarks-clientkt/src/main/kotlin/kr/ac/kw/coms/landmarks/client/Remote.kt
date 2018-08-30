package kr.ac.kw.coms.landmarks.client

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.beust.klaxon.internal.firstNotNullResult
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.engine.android.Android
import io.ktor.client.features.cookies.AcceptAllCookiesStorage
import io.ktor.client.features.cookies.HttpCookies
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.*
import io.ktor.client.response.HttpResponse
import io.ktor.http.*
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.sendBlocking
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.io.jvm.javaio.toOutputStream
import java.io.File
import java.util.*
import kotlin.math.max

class Remote(base: HttpClient, val basePath: String = herokuUri) {

  val http: HttpClient
  val nominatimReqLimit = Channel<Long>(1)

  companion object {
    const val herokuUri = "https://landmarks-coms.herokuapp.com"
    private const val chromeAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.59 Safari/537.36"
  }

  suspend inline fun <reified T> request(method: HttpMethod, url: String, builder: HttpRequestBuilder.() -> Unit = {}): T {
    val response: HttpResponse = http.request {
      this.method = method
      url(url)
      builder()
    }
    if (response.status.isSuccess()) {
      return response.call.receive()
    }
    throw response.call.receive<ServerFault>()
  }

  suspend inline fun <reified T> get(url: String, builder: HttpRequestBuilder.() -> Unit = {}): T =
    request(HttpMethod.Get, url, builder)

  suspend inline fun <reified T> post(url: String, builder: HttpRequestBuilder.() -> Unit = {}): T =
    request(HttpMethod.Post, url, builder)

  suspend inline fun <reified T> put(url: String, builder: HttpRequestBuilder.() -> Unit = {}): T =
    request(HttpMethod.Put, url, builder)

  init {
    nominatimReqLimit.sendBlocking(0)
    http = base.config {
      install(HttpCookies) {
        storage = AcceptAllCookiesStorage()
      }
      install(JsonFeature) {
        serializer = KlaxonSerializer()
      }
    }
  }

  constructor() : this(HttpClient(Android.create()), herokuUri)

  private fun HttpRequestBuilder.userAgent() {
    header("User-Agent", "landmarks-client")
  }

  private fun HttpRequestBuilder.json(json: Any) {
    contentType(ContentType.Application.Json)
    body = json
  }

  suspend fun suspendForNominatimReqPerSecLimit() {
    val last: Long = nominatimReqLimit.receive()
    nominatimReqLimit.offer(Date().time)
    delay(max(0, last + 1000 - Date().time))
  }

  suspend fun reverseGeocode(latitude: Double, longitude: Double): Pair<String?, String?>? {
    suspendForNominatimReqPerSecLimit()

    val json: String = get("https://nominatim.openstreetmap.org/reverse") {
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

  suspend fun checkAlive(): Boolean {
    try {
      get<Unit>("$basePath/")
      return true
    } catch (e: Throwable) {
      return false
    }
  }

  suspend fun resetAllDatabase() {
    put<Unit>("$basePath/maintenance/reset")
  }

  suspend fun register(ident: String, pass: String, email: String, nick: String) {
    val regFields = LoginRep(
      login = ident,
      password = pass,
      email = email,
      nick = nick
    )
    post<Unit>("$basePath/auth/register") {
      userAgent()
      json(regFields)
    }
  }

  suspend fun login(ident: String, pass: String) {
    val par = LoginRep(login = ident, password = pass)
    val profile: LoginRep = post("$basePath/auth/login") {
      userAgent()
      json(par)
    }
  }

  suspend fun uploadPicture(file: File, latitude: Float? = null, longitude: Float? = null, addr: String? = null) {
    val content = MultiPartContent.build {
      latitude?.also { add("lat", it.toString()) }
      longitude?.also { add("lon", it.toString()) }
      addr?.also { add("address", it) }
      add("pic0", filename = file.name) {
        file.inputStream().copyToSuspend(toOutputStream())
      }
    }
    put<Unit>("$basePath/picture") {
      body = content
    }
  }

  suspend fun getRandomProblem(): PictureRep {
    val pic: PictureRep = get("$basePath/problem/random")
    pic.file = get("$basePath/picture/${pic.id}")
    return pic
  }

  suspend fun getMyPictures() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  suspend fun getOtherPictures(userId: Int) {
  }

  suspend fun getMyCollections() {

  }

}
