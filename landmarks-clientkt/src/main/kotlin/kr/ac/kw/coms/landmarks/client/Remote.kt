package kr.ac.kw.coms.landmarks.client

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.cookies.AcceptAllCookiesStorage
import io.ktor.client.features.cookies.HttpCookies
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.*
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.append
import io.ktor.client.request.forms.formData
import io.ktor.client.response.HttpResponse
import io.ktor.http.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.io.readUTF8LineTo
import kotlinx.io.core.writeFully
import java.io.File
import java.io.InputStream
import java.net.URLEncoder
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max

private typealias HRB = HttpRequestBuilder.() -> Unit

class Remote(engine: HttpClient, private val basePath: String = herokuUri) {
  /*
  Implementation details: method's signature MutableList should be kept.
  Gson can't aware List<> in deserialization type detection.
   */

  val http: HttpClient
  var logger: RemoteLoggable? = null
  var profile: IdAccountForm? = null

  private val nominatimLastRequestMs = Channel<Long>(1)

  companion object {
    const val herokuUri = "http://landmarks-coms.herokuapp.com"
    private const val chromeAgent =
      "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.59 Safari/537.36"
  }

  constructor() : this(HttpClient(OkHttp.create {
    config {
      connectTimeout(1, TimeUnit.MINUTES)
      writeTimeout(1, TimeUnit.MINUTES)
      readTimeout(1, TimeUnit.MINUTES)
    }
  }))

  init {
    nominatimLastRequestMs.sendBlocking(0)
    http = engine.config {
      install(HttpCookies) {
        storage = AcceptAllCookiesStorage()
      }
      install(JsonFeature) {
      }
    }
  }

  private suspend inline fun <reified T> request(
    method: HttpMethod,
    url: String,
    builder: HttpRequestBuilder.() -> Unit = {}
  ): T {
    logger?.onRequest("${method.value} $url HTTP/1.1")
    val response: HttpResponse = http.request {
      this.method = method
      url(url)
      header("User-Agent", "landmarks-client")
      builder()
    }
    if (response.status.isSuccess()) {
      logger?.onResponseSuccess("${method.value} $url HTTP/1.1")
      return response.call.receive()
    }
    if (response.status == HttpStatusCode.BadRequest) {
      logger?.onResponseFailure("${method.value} $url HTTP/1.1")
      throw response.call.receive<ServerFault>()
    }
    else {
      val sb = StringBuilder()
      while (response.content.readUTF8LineTo(sb));
      val msg = "${response.status}: $sb"
      throw RuntimeException(msg)
    }
  }

  private suspend inline fun <reified T> get(url: String, builder: HRB = {}): T =
    request(HttpMethod.Get, url, builder)

  private suspend inline fun <reified T> post(url: String, builder: HRB = {}): T =
    request(HttpMethod.Post, url, builder)

  private suspend inline fun <reified T> put(url: String, builder: HRB = {}): T =
    request(HttpMethod.Put, url, builder)

  private suspend inline fun <reified T> delete(url: String, builder: HRB = {}): T =
    request(HttpMethod.Delete, url, builder)

  private fun HttpRequestBuilder.json(json: Any) {
    contentType(ContentType.Application.Json)
    body = json
  }

  suspend fun reverseGeocode(latitude: Double, longitude: Double): ReverseGeocodeResult {
    val last: Long = nominatimLastRequestMs.receive()
    delay(max(0, last + 1000 - Date().time))

    val ret: ReverseGeocodeResult = reverseGeocodeUnsafe(latitude, longitude)

    val next: Long = Date().time + 1000
    nominatimLastRequestMs.send(next)

    return ret
  }

  private suspend fun reverseGeocodeUnsafe(
    latitude: Double,
    longitude: Double
  ): ReverseGeocodeResult {
    val json: String = get("https://nominatim.openstreetmap.org/reverse") {
      parameter("format", "json")
      parameter("accept-language", "ko,en")
      parameter("lat", latitude.toString())
      parameter("lon", longitude.toString())
      userAgent(chromeAgent)
    }
    val obj: JsonObject = Parser().parse(StringBuilder(json)) as JsonObject
    return ReverseGeocodeResult(obj)
  }

  suspend fun checkAlive(): Boolean {
    return try {
      get<Unit>("$basePath/")
      true
    }
    catch (e: Exception) {
      false
    }
  }

  suspend fun resetAllDatabase() {
    get<Unit>("$basePath/maintenance/reset")
  }

  suspend fun register(ident: String, pass: String, email: String, nick: String) {
    val regFields = AccountForm(
      login = ident,
      password = pass,
      email = email,
      nick = nick
    )
    post<Unit>("$basePath/auth/register") {
      json(regFields)
    }
  }

  suspend fun login(ident: String, pass: String): IdAccountForm {
    profile = post("$basePath/auth/login") {
      json(AccountForm(login = ident, password = pass))
    }
    return profile!!
  }

  suspend fun uploadPicture(meta: IPictureInfo, file: File): IdPictureInfo {
    val filename: String = URLEncoder.encode(file.name, "UTF-8")
    val form = MultiPartFormDataContent(formData {
      meta.lat?.also { append("lat", it.toString()) }
      meta.lon?.also { append("lon", it.toString()) }
      meta.address?.also { append("address", it) }
      append("pic0", filename) {
        writeFully(file.readBytes())
      }
    })
    return post("$basePath/picture") {
      body = form
    }
  }

  suspend fun getPictures(cond: PictureQuery?): MutableList<IdPictureInfo> {
    return get("$basePath/picture?$cond")
  }

  suspend fun getRandomPictures(n: Int): List<IdPictureInfo> {
    return get("$basePath/picture/random?n=$n")
  }

  suspend fun getPictureInfo(id: Int): PictureInfo {
    return get("$basePath/picture/info/$id")
  }

  suspend fun getPicture(id: Int): InputStream {
    return get("$basePath/picture/$id")
  }

  suspend fun getThumbnail(
    id: Int,
    desiredWidth: Int = 640,
    desiredHeight: Int = 480
  ): InputStream {
    return get("$basePath/picture/thumbnail/$id?width=$desiredWidth&height=$desiredHeight")
  }

  suspend fun modifyPictureInfo(id: Int, info: IPictureInfo) {
    return put("$basePath/picture/info/$id") {
      json(info)
    }
  }

  suspend fun deletePicture(id: Int) {
    return delete("$basePath/picture/$id")
  }

  suspend fun uploadCollection(collection: CollectionInfo): IdCollectionInfo {
    return post("$basePath/collection") {
      json(collection)
    }
  }

  suspend fun getRandomCollections(): MutableList<IdCollectionInfo> {
    return get("$basePath/collection")
  }

  suspend fun getCollections(ownerId: Int): MutableList<IdCollectionInfo> {
    return get("$basePath/collection/user/$ownerId")
  }

  suspend fun getMyCollections(): MutableList<IdCollectionInfo> {
    return getCollections(profile!!.id)
  }

  suspend fun getCollectionsContainPicture(picId: Int): MutableList<IdCollectionInfo> {
    return get("$basePath/collection/contains/picture/$picId")
  }

  suspend fun modifyCollection(id: Int, collection: CollectionInfo): IdCollectionInfo {
    return put("$basePath/collection/$id") {
      json(collection)
    }
  }

  suspend fun deleteCollection(id: Int) {
    return delete("$basePath/collection/$id")
  }

  suspend fun getProfile(): ProfileInfo {
    return get("$basePath/profile")
  }
}
