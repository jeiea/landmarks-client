package kr.ac.kw.coms.landmarks.client

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.engine.android.Android
import io.ktor.client.features.cookies.AcceptAllCookiesStorage
import io.ktor.client.features.cookies.HttpCookies
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.*
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.response.HttpResponse
import io.ktor.http.*
import kotlinx.coroutines.experimental.channels.ArrayChannel
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.sendBlocking
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.io.readUTF8LineTo
import kotlinx.coroutines.experimental.launch
import kotlinx.io.InputStream
import kotlinx.io.core.writeFully
import java.io.File
import java.net.URLEncoder
import java.util.*
import kotlin.math.max

class Remote(base: HttpClient, val basePath: String = herokuUri) {
  /*
  Implementation details: method's signature MutableList should be kept.
  Gson can't aware List<> in deserialization type detection.
   */

  val http: HttpClient
  private val nominatimLastRequestMs = ArrayChannel<Long>(1)
  private val problemBuffer = Channel<IdPictureInfo>(10)
  private val problemBuffering by lazy {
    launch {
      while (true) {
        val pics: MutableList<IdPictureInfo> = get("$basePath/problem/random/10")
        pics.forEach { problemBuffer.send(it) }
      }
    }
  }

  companion object {
    const val herokuUri = "http://landmarks-coms.herokuapp.com"
    private const val chromeAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.59 Safari/537.36"
  }

  var profile: IdAccountForm? = null

  private suspend inline fun <reified T> request(method: HttpMethod, url: String, builder: HttpRequestBuilder.() -> Unit = {}): T {
    val response: HttpResponse = http.request {
      this.method = method
      url(url)
      header("User-Agent", "landmarks-client")
      builder()
    }
    if (response.status.isSuccess()) {
      return response.call.receive()
    }
    if (response.status == HttpStatusCode.BadRequest) {
      throw response.call.receive<ServerFault>()
    } else {
      val sb = StringBuilder()
      while (response.content.readUTF8LineTo(sb));
      val msg = "${response.status}: $sb"
      throw RuntimeException(msg)
    }
  }

  private suspend inline fun <reified T> get(url: String, builder: HttpRequestBuilder.() -> Unit = {}): T =
    request(HttpMethod.Get, url, builder)

  private suspend inline fun <reified T> post(url: String, builder: HttpRequestBuilder.() -> Unit = {}): T =
    request(HttpMethod.Post, url, builder)

  private suspend inline fun <reified T> put(url: String, builder: HttpRequestBuilder.() -> Unit = {}): T =
    request(HttpMethod.Put, url, builder)

  init {
    nominatimLastRequestMs.sendBlocking(0)
    http = base.config {
      install(HttpCookies) {
        storage = AcceptAllCookiesStorage()
      }
      install(JsonFeature) {
      }
    }
  }

  constructor() : this(HttpClient(Android.create()), herokuUri)

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

  private suspend fun reverseGeocodeUnsafe(latitude: Double, longitude: Double): ReverseGeocodeResult {
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
    } catch (e: Throwable) {
      false
    }
  }

  suspend fun resetAllDatabase() {
    put<Unit>("$basePath/maintenance/reset")
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
    return put("$basePath/picture") {
      body = form
    }
  }

  suspend fun getRandomProblems(n: Int): MutableList<IdPictureInfo> {
    problemBuffering
    val ret = mutableListOf<IdPictureInfo>()
    for (i in 1..n) {
      ret.add(problemBuffer.receive())
    }
    return ret
  }

  suspend fun modifyPictureInfo(id: Int, info: IPictureInfo) {
    return post("$basePath/picture/info/$id") {
      json(info)
    }
  }

  suspend fun getPictureInfo(id: Int): PictureInfo {
    return get("$basePath/picture/info/$id")
  }

  suspend fun deletePicture(id: Int) {
    TODO()
  }

  suspend fun getPicture(id: Int): InputStream {
    return get("$basePath/picture/$id")
  }

  suspend fun getThumbnail(id: Int, desiredWidth: Int = 640, desiredHeight: Int = 480): InputStream {
    return get("$basePath/picture/thumbnail/$id?width=$desiredWidth&height=$desiredHeight")
  }

  suspend fun getPictureInfos(userId: Int): MutableList<IdPictureInfo> {
    return get("$basePath/picture/user/$userId")
  }

  suspend fun getMyPictureInfos(): MutableList<IdPictureInfo> {
    return getPictureInfos(profile!!.id)
  }


  suspend fun uploadCollection(collection: ICollectionInfo): IdCollectionInfo {
    return put("$basePath/collection") {
      json(collection)
    }
  }

  suspend fun getRandomCollections(): MutableList<IdCollectionInfo> {
    return get("$basePath/collection")
  }

  suspend fun getCollections(ownerId: Int): MutableList<IdCollectionInfo> {
    return get("$basePath/collection/user/$ownerId")
  }

  suspend fun getCollectionPics(collectionId: Int): MutableList<IdPictureInfo> {
    return get("$basePath/collection/$collectionId/picture")
  }

  suspend fun getMyCollections(): MutableList<IdCollectionInfo> {
    return getCollections(profile!!.id)
  }

  suspend fun getCollectionsContainPicture(picId: Int): MutableList<IdCollectionInfo> {
    return get("$basePath/collection/contains/picture/$picId")
  }

  suspend fun modifyCollection(id: Int, collection: ICollectionInfo): IdCollectionInfo {
    return post("$basePath/collection/$id") {
      json(collection)
    }
  }

  suspend fun deleteCollection(id: Int) {
    TODO()
  }
}
