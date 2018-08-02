package kr.ac.kw.coms.globealbum.provider

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.engine.config
import io.ktor.client.features.cookies.AcceptAllCookiesStorage
import io.ktor.client.features.cookies.HttpCookies
import kotlinx.coroutines.experimental.runBlocking
import org.amshove.kluent.`should be equal to`
import org.apache.http.HttpHost
import org.apache.http.conn.ssl.TrustSelfSignedStrategy
import org.apache.http.ssl.SSLContextBuilder
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.TestBody
import org.jetbrains.spek.api.dsl.TestContainer
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
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

    blit("does reverse geocoding") {
      val s = client.reverseGeocode(37.54567, 126.9944)
      s!!.first!! `should be equal to` "대한민국"
      s.second!! `should be equal to` "서울특별시"
    }

    blit("checks server health") {
      client.checkAlive()
    }

    val ident = getRandomString(8)
    blit("registers a user") {
      client.register(ident, "pasowo", "$ident@b.c", ident)
    }

    blit("does login") {
      client.login(ident, "pasowo")
    }

    blit("uploads picture") {
      client.uploadPic(File("../honeyview_gps.jpg"), 3.3f, 1f, "somewhere")
    }
  }
})

fun TestContainer.blit(description: String, body: suspend TestBody.() -> Unit) {
  it(description) {
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
  System.setProperty(vmoUseProxy, "true");
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