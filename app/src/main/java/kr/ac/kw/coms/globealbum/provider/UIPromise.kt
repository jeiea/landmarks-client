package kr.ac.kw.coms.globealbum.provider

import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

open class UIPromise<T> : Promise<T>() {
  override fun resolve(scope: CoroutineScope, block: suspend () -> T): Job {
    return scope.launch(UI) {
      try {
        resolve(block())
      }
      catch (e: Throwable) {
        resolve(e)
      }
    }
  }
}
