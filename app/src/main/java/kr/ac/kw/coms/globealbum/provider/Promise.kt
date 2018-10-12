package kr.ac.kw.coms.globealbum.provider

import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.coroutineContext

open class Promise<T> {
  var ans: T? = null
  var err: Throwable? = null

  open fun success(result: T) {
    ans = result
  }

  open fun failure(cause: Throwable) {
    err = cause
  }

  fun resolve(result: T): Unit = success(result)
  fun resolve(cause: Throwable): Unit = failure(cause)
  suspend fun resolve(block: suspend () -> T): Job =
    resolve(CoroutineScope(coroutineContext), block)

  open fun resolve(scope: CoroutineScope, block: suspend () -> T): Job = scope.launch {
    try {
      resolve(block())
    }
    catch (e: Throwable) {
      resolve(e)
    }
  }
}