package kr.ac.kw.coms.globealbum.provider

open class Promise<T> {
  var ans: T? = null
  var err: Throwable? = null

  open fun success(result: T) {
    ans = result
  }

  open fun failure(cause: Throwable) {
    err = cause
  }

  open suspend fun resolve(block: suspend () -> T) {
    success(
      try {
        block()
      }
      catch (e: Throwable) {
        failure(e)
        return
      }
    )
  }
}