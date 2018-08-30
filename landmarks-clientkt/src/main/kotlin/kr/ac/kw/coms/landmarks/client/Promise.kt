package kr.ac.kw.coms.landmarks.client

import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.runBlocking

open class Promise<T> {
  var ans: T? = null
  var err: Throwable? = null

  open fun success(result: T) {
    ans = result
  }

  open fun failure(cause: Throwable) {
    err = cause
  }

  open fun resolve(result: T) = success(result)
  open fun resolve(cause: Throwable) = failure(cause)
  open fun resolve(block: suspend CoroutineScope.() -> T) {
    resolve(runBlocking(block = block))
  }
}