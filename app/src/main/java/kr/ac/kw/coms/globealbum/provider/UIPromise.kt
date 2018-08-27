package kr.ac.kw.coms.globealbum.provider

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import kr.ac.kw.coms.landmarks.client.Promise

open class UIPromise<T> : Promise<T>() {
  override fun resolve(block: suspend CoroutineScope.() -> T) {
    runBlocking {
      launch(UI) {
        try {
          resolve(async(CommonPool, block = block).await())
        } catch (e: Throwable) {
          resolve(e)
        }
      }
    }
  }
}
