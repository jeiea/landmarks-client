package kr.ac.kw.coms.globealbum.provider

import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.coroutineScope
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext

open class UIPromise<T> : Promise<T>() {
  override suspend fun resolve(block: suspend () -> T) = coroutineScope {
    val outer = coroutineContext
    launch(Dispatchers.Main) {
      success(
        try {
          withContext(outer) { block() }
        }
        catch (e: Throwable) {
          failure(e)
          return@launch
        }
      )
    }
    Unit
  }
}
