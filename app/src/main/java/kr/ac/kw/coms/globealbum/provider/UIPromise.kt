package kr.ac.kw.coms.globealbum.provider

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI

open class UIPromise<T> : Promise<T>() {
  override fun resolve(block: suspend CoroutineScope.() -> T): Job {
    val background: Deferred<T> = async(CommonPool, block = block)
    launch(UI) {
      try {
        resolve(background.await())
      } catch (e: Throwable) {
        resolve(e)
      }
    }
    return background
  }
}
