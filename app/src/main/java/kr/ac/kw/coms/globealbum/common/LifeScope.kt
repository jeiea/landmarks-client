package kr.ac.kw.coms.globealbum.common

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob

/**
 * 안드로이드 생명주기 객체에 맞춰 비동기 작업을 종료시키는 클래스. 안드로이드 액티비티로도 생성
 * 가능
 * @see android.app.Activity
 */
class LifeScope(owner: LifecycleOwner) : CoroutineScope {
  protected val life = SupervisorJob()
  @ExperimentalCoroutinesApi
  override val coroutineContext = Dispatchers.Main.immediate + life

  init {
    owner.lifecycle.addObserver(object : LifecycleObserver {
      @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
      fun onDestroy() {
        life.cancel()
      }
    })
  }
}

interface Disposable {
  fun dispose()
}