package kr.ac.kw.coms.globealbum.common

import io.ktor.http.*
import io.ktor.http.content.OutgoingContent
import io.ktor.network.util.ioCoroutineDispatcher
import io.ktor.util.flattenEntries
import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.io.ByteWriteChannel
import kotlinx.coroutines.experimental.io.writeFully
import kotlinx.coroutines.experimental.io.writeStringUtf8
import kotlinx.coroutines.experimental.withContext
import kotlinx.coroutines.experimental.yield
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class MultiPartContent(val parts: List<Part>) : OutgoingContent.WriteChannelContent() {
  val uuid = UUID.randomUUID()
  val boundary = "***$uuid--${System.currentTimeMillis()}***"

  data class Part(val name: String, val filename: String? = null, val headers: Headers = Headers.Empty, val writer: suspend ByteWriteChannel.() -> Unit)

  override suspend fun writeTo(channel: ByteWriteChannel) {
    for (part: Part in parts) {
      channel.writeStringUtf8("--$boundary\r\n")
      val partHeaders = Headers.build {
        val fileNamePart = if (part.filename != null) "; filename=\"${part.filename}\"" else ""
        append("Content-Disposition", "form-data; name=\"${part.name}\"$fileNamePart")
        appendAll(part.headers)
      }
      for ((key, value) in partHeaders.flattenEntries()) {
        channel.writeStringUtf8("$key: $value\r\n")
      }
      channel.writeStringUtf8("\r\n")
      part.writer(channel)
      channel.writeStringUtf8("\r\n")
    }
    channel.writeStringUtf8("--$boundary--\r\n")
  }

  override val contentType = ContentType.MultiPart.FormData
    .withParameter("boundary", boundary)
    .withCharset(Charsets.UTF_8)

  class Builder {
    val parts = arrayListOf<Part>()

    fun add(part: Part) {
      parts += part
    }

    fun add(name: String, filename: String? = null, contentType: ContentType? = null, headers: Headers = Headers.Empty, writer: suspend ByteWriteChannel.() -> Unit) {
      val contentTypeHeaders: Headers = if (contentType != null) headersOf(HttpHeaders.ContentType, contentType.toString()) else headersOf()
      add(Part(name, filename, headers + contentTypeHeaders, writer))
    }

    fun add(name: String, text: String, contentType: ContentType? = null, filename: String? = null) {
      add(name, filename, contentType) { writeStringUtf8(text) }
    }

    fun add(name: String, data: ByteArray, contentType: ContentType? = ContentType.Application.OctetStream, filename: String? = null) {
      add(name, filename, contentType) { writeFully(data) }
    }

    internal fun build(): MultiPartContent = MultiPartContent(parts.toList())
  }

  companion object {
    fun build(callback: Builder.() -> Unit) = Builder().apply(callback).build()
  }
}

operator fun Headers.plus(other: Headers): Headers = when {
  this.isEmpty() -> other
  other.isEmpty() -> this
  else -> Headers.build {
    appendAll(this@plus)
    appendAll(other)
  }
}

suspend fun InputStream.copyToSuspend(
  out: OutputStream,
  bufferSize: Int = DEFAULT_BUFFER_SIZE,
  yieldSize: Int = 4 * 1024 * 1024,
  dispatcher: CoroutineDispatcher = ioCoroutineDispatcher
): Long {
  return withContext(dispatcher) {
    val buffer = ByteArray(bufferSize)
    var bytesCopied = 0L
    var bytesAfterYield = 0L
    while (true) {
      val bytes = read(buffer).takeIf { it >= 0 } ?: break
      out.write(buffer, 0, bytes)
      if (bytesAfterYield >= yieldSize) {
        yield()
        bytesAfterYield %= yieldSize
      }
      bytesCopied += bytes
      bytesAfterYield += bytes
    }
    return@withContext bytesCopied
  }
}
