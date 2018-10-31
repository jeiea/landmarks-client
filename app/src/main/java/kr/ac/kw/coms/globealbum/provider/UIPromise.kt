package kr.ac.kw.coms.globealbum.provider

import kotlinx.coroutines.experimental.*

open class UIPromise<T> : Promise<T>() {
  override fun resolve(scope: CoroutineScope, block: suspend () -> T): Job {
    return scope.launch(Dispatchers.Main) {
      try {
        resolve(withContext(scope.coroutineContext) { block() })
      }
      catch (e: Throwable) {
        resolve(e)
      }
    }
  }
}
