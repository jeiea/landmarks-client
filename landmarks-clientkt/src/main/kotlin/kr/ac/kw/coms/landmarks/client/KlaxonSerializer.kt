package kr.ac.kw.coms.landmarks.client

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import io.ktor.client.call.TypeInfo
import io.ktor.client.features.json.JsonSerializer
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent

class KlaxonSerializer(block: Klaxon.() -> Unit = {}) : JsonSerializer {

  private val backend: Klaxon = Klaxon().apply(block)

  override fun write(data: Any): OutgoingContent =
    TextContent(backend.toJsonString(data), ContentType.Application.Json)

  override suspend fun read(type: TypeInfo, response: HttpResponse): Any {
    val text: String = response.readText()
    val json = backend.parser(type.type).parse(StringBuilder(text)) as JsonObject
    return backend.fromJsonObject(json, type.javaClass, type.type)
  }
}
