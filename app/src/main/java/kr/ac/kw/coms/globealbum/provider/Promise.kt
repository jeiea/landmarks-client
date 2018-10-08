package kr.ac.kw.coms.globealbum.provider

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.CoroutineContext

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
  open fun resolve(
    context: CoroutineContext = CommonPool,
    block: suspend CoroutineScope.() -> T)
    : Job = launch(context) { resolve(block()) }
}