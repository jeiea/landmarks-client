package kr.ac.kw.coms.globealbum.provider

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
