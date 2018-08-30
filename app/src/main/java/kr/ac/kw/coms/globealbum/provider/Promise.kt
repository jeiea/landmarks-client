package kr.ac.kw.coms.globealbum.provider

import kotlinx.coroutines.experimental.*

open class Promise<T> {
  var ans: T? = null
  var err: Throwable? = null

  open fun success(result: T) {
    ans = result
  }

  open fun failure(cause: Throwable) {
    err = cause
  }

  open fun resolve(result: T): Unit = success(result)
  open fun resolve(cause: Throwable): Unit = failure(cause)
  open fun resolve(block: suspend CoroutineScope.() -> T): Job = launch {
    resolve(block())
  }
}