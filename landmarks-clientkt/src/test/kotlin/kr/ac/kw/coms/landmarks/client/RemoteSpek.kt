package kr.ac.kw.coms.landmarks.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.engine.config
import io.ktor.client.features.cookies.AcceptAllCookiesStorage
import io.ktor.client.features.cookies.HttpCookies
import kotlinx.coroutines.experimental.runBlocking
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be greater than`
import org.amshove.kluent.`should be true`
import org.amshove.kluent.`should not be equal to`
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
  describe("landmarks client") {
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

    var profile: LoginRep? = null
    blit("does login") {
      val p = client.login(ident, "pasowo")
      p.login!! `should be equal to` ident
      p.email!! `should be equal to` email
      p.nick!! `should be equal to` ident
      profile = p
    }

    blit("uploads picture") {
      for (i in 0..3) {
        client.uploadPicture(File("../data/coord$i.jpg"), i.toFloat(), i.toFloat(), "address$i")
      }
    }

    val pics: ArrayList<PictureRep> = arrayListOf()
    blit("receives quiz info") {
      pics.addAll(client.getRandomProblems(2))
      pics[0].id `should not be equal to` pics[1].id
    }

    blit("download picture") {
      client.getPicture(pics[1].id).readBytes().size `should be greater than` 0
    }

    blit("query user's pictures") {
      TODO()
    }

    blit("query user's collections") {
      TODO()
    }

    blit("query a collection") {
      TODO()
    }
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