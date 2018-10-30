package kr.ac.kw.coms.globealbum.common

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.SupervisorJob

class LifeScope(owner: LifecycleOwner) :
  CoroutineScope {
  protected val life = SupervisorJob()
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