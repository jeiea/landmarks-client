package kr.ac.kw.coms.landmarks.client

import io.ktor.client.HttpClient
import io.ktor.client.features.HttpClientFeature
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.util.AttributeKey

/**
 * Incomplete. Filed an issue https://github.com/ktorio/ktor/issues/537
 */
class HttpBasePath(val basePath: String?) {
  class Config {
    var basePath: String? = null

    fun build() = HttpBasePath(basePath)
  }

  companion object : HttpClientFeature<Config, HttpBasePath> {
    override val key = AttributeKey<HttpBasePath>("HttpBasePath")

    override fun prepare(block: Config.() -> Unit): HttpBasePath = Config().apply(block).build()

    override fun install(feature: HttpBasePath, scope: HttpClient) {
      scope.requestPipeline.intercept(HttpRequestPipeline.Before) { _ ->
        if (feature.basePath != null) {
          context.url.host = feature.basePath
        }
      }
    }
  }
}