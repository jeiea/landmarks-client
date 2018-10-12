package kr.ac.kw.coms.landmarks.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.engine.config
import io.ktor.client.features.cookies.AcceptAllCookiesStorage
import io.ktor.client.features.cookies.HttpCookies
import kotlinx.coroutines.experimental.runBlocking
import org.amshove.kluent.*
import org.apache.http.HttpHost
import org.apache.http.conn.ssl.TrustSelfSignedStrategy
import org.apache.http.ssl.SSLContextBuilder
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.io.File
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.ProxySelector
import java.net.URI
import java.util.*

@RunWith(JUnitPlatform::class)
class RemoteSpek : Spek({
  describe("landmarks server single user") {
    //    val client = Remote(getTestClient(), "http://landmarks-coms.herokuapp.com/")
    val client = Remote(getTestClient(), "http://localhost:8080")

    xblit("does reverse geocoding") {
      val res: ReverseGeocodeResult = client.reverseGeocode(37.54567, 126.9944)
      res.country!! `should be equal to` "대한민국"
      res.detail!! `should be equal to` "서울특별시"
    }

    blit("checks server health") {
      client.checkAlive().`should be true`()
    }

    blit("reset all DB") {
      client.resetAllDatabase()
    }

    val ident = getRandomString(8)
    val pass = "pasowo"
    val email = "$ident@b.c"
    blit("registers a user") {
      client.register(ident, pass, email, ident)
    }

    var profile: AccountForm? = null
    blit("does login") {
      val p = client.login(ident, "pasowo").data
      p.login!! `should be equal to` ident
      p.email!! `should be equal to` email
      p.nick!! `should be equal to` ident
      profile = p
    }

    val pics = mutableListOf<IdPictureInfo>()
    blit("uploads picture") {
      val jpgs = File("../../landmarks-data/archive0").listFiles()
      for ((idx, f) in jpgs.withIndex()) {
        val gps = idx.toFloat() * 3
        val info = PictureInfo(lat = gps, lon = gps, address = f.nameWithoutExtension)
        val pic = client.uploadPicture(info, f)
        pics.add(pic)
      }
    }

    blit("download picture") {
      client.getPicture(pics[0].id).readBytes().size `should be greater than` 3000
    }

    var replaced = PictureInfo()
    blit("modify picture info") {
      replaced = pics[0].data.copy(address = "Manhatan?", lat = 20.0f, lon = 110.0f)
      client.modifyPictureInfo(pics[0].id, replaced)
    }

    blit("receive picture info") {
      val modified: PictureInfo = client.getPictureInfo(pics[0].id)
      val rep = replaced
      modified.address!! `should be equal to` rep.address!!
      modified.lat!! `should be equal to` rep.lat!!
      modified.lon!! `should be equal to` rep.lon!!
    }

    blit("query my pictures") {
      client.getMyPictureInfos().size `should be equal to` 3
    }

    blit("receives quiz info") {
      val quizs = mutableListOf<IdPictureInfo>()
      quizs.addAll(client.getRandomProblems(2))
      quizs[0].id `should not be equal to` quizs[1].id
    }

    blit("query thumbnail") {
      val stream = client.getThumbnail(pics[0].id, 400, 200)
      stream.readBytes().size `should be less than` 200000
    }

    blit("query around pictures") {
      val ps = client.getAroundPictures(21.0, 111.0, 300.0)
      ps.size `should be greater than` 0
    }

    // Deletion of picture is not yet implemented.

    val collection = CollectionInfo(
      title = "first diary",
      text = "just first"
    )
    var coll: IdCollectionInfo? = null
    blit("upload collections") {
      coll = client.uploadCollection(collection)
    }

    blit("modify collections") {
      collection.images = ArrayList(pics.map { it.id })
      coll = client.modifyCollection(coll!!.id, collection)
      coll!!.data.previews!!.size `should be equal to` collection.images!!.size
      collection.images!!.remove(0)
      coll = client.modifyCollection(coll!!.id, collection)
      coll!!.data.images!!.size `should be equal to` collection.images!!.size
    }

    var createdCollId = 0
    blit("query my collections") {
      val queried = client.getMyCollections()
      queried.size `should be equal to` 1

      val collGot: CollectionInfo = queried[0].data
      collGot.images!! `should equal` collection.images!!
      collGot.previews!!.size `should be equal to`  collection.images!!.size

      createdCollId = queried[0].id
    }

    blit("query collections by a picture") {
      val colls = client.getCollectionsContainPicture(pics[0].id)
      colls.size `should be equal to` 1
    }

    blit("get random collections") {
      val queried = client.getRandomCollections()
      // it filters myself one
      queried.size `should be equal to` 0
    }

    // Deletion of collection is not yet implemented
  }
})

fun TestContainer.blit(description: String, body: suspend TestBody.() -> Unit) {
  it(description) {
    runBlocking { body() }
  }
}

fun TestContainer.xblit(description: String, body: suspend TestBody.() -> Unit) {
  xit(description) {
    runBlocking { body() }
  }
}

fun getTestClient(): HttpClient {
  return HttpClient(Apache.config {
    customizeClient {
      setProxy(HttpHost("localhost", 8888))
      val sslContext = SSLContextBuilder().loadTrustMaterial(null,
        TrustSelfSignedStrategy.INSTANCE).build()
      setSSLContext(sslContext)
    }
    socketTimeout = 0
    connectTimeout = 0
    connectionRequestTimeout = 0

  }) {
    install(HttpCookies) {
      storage = AcceptAllCookiesStorage()
    }
  }
}

fun getSystemProxy(): InetSocketAddress? {
  val vmoUseProxy = "java.net.useSystemProxies"
  System.setProperty(vmoUseProxy, "true")
  val proxies = ProxySelector
    .getDefault().select(URI("http://localhost"))
  for (proxy: Proxy in proxies) {
    println("proxy: $proxy")
    return proxy.address() as InetSocketAddress
  }
  return null
}

fun getRandomString(length: Long): String {
  val source = "abcdefghijklmnopqrstuvwxyz0123456789"
  return Random()
    .ints(length, 0, source.length)
    .mapToObj(source::get)
    .toArray()
    .joinToString("")
}