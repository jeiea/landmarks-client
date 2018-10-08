package kr.ac.kw.coms.landmarks.client

interface RemoteLoggable {
  fun onRequest(msg: String)
  fun onResponseSuccess(msg: String)
  fun onResponseFailure(msg: String)
}
