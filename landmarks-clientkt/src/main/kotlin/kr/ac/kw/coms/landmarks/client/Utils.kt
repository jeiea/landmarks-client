package kr.ac.kw.coms.landmarks.client

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.InputStream
import java.io.OutputStream
import kotlin.reflect.KProperty

suspend fun InputStream.copyToSuspend(
  out: OutputStream,
  bufferSize: Int = DEFAULT_BUFFER_SIZE,
  yieldSize: Int = 4 * 1024 * 1024,
  dispatcher: CoroutineDispatcher = Dispatchers.IO
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

fun getThumbnailLevel(
  picWidth: Int, picHeight: Int,
  desireWidth: Int, desireHeight: Int
): Int {

  var width = picWidth
  var height = picHeight
  for (i in 0..3) {
    width /= 2
    height /= 2
    if (width < desireWidth || height < desireHeight) {
      return i
    }
  }
  return 4
}

class RecoverableChannel<T>(val block: () -> ReceiveChannel<T>) {
  var channel: ReceiveChannel<T>? = null

  private fun resetChannel(): ReceiveChannel<T> {
    return block().also { channel = it }
  }

  operator fun getValue(thisRef: Any?, property: KProperty<*>): ReceiveChannel<T> {
    return channel?.takeIf { !it.isClosedForReceive } ?: resetChannel()
  }
}
