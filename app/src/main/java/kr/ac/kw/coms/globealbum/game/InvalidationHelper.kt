package kr.ac.kw.coms.globealbum.game

import android.os.Handler
import android.os.SystemClock
import android.view.View

/**
 * invalidate()를 초당 60번 이상 호출하면 손해겠지?
 * 프레임 제한을 걸기 위한 클래스.
 */
internal class InvalidationHelper(
  val handler: Handler,
  val view: View,
  val msInterval: Long) : Runnable {

  var queued: Boolean = false
  var lastRequest: Long = 0

  fun postInvalidate() {
    val now = SystemClock.uptimeMillis()
    val next = lastRequest + msInterval
    if (now > next) {
      lastRequest = now
      view.invalidate()
    } else if (!queued) {
      queued = true
      handler.postDelayed(this, next - now)
    }
  }

  override fun run() {
    postInvalidate()
    queued = false
  }
}
