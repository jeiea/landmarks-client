package kr.ac.kw.coms.globealbum.provider

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import kotlinx.android.synthetic.main.layout_login.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.sync.Mutex
import kotlinx.coroutines.experimental.sync.withLock
import kr.ac.kw.coms.globealbum.MainActivity
import kr.ac.kw.coms.globealbum.R
import kr.ac.kw.coms.globealbum.common.app
import org.jetbrains.anko.contentView
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onKey
import org.jetbrains.anko.toast


class LoginActivity : AppCompatActivity(), CoroutineScope {
  protected val life = SupervisorJob()
  override val coroutineContext = Dispatchers.Main.immediate + life

  private val loginMutex = Mutex()
  private var loggingIn: Job? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.layout_splash)
    contentView?.postDelayed(::displayLoginOrPass, 1000)
  }

  override fun onDestroy() {
    super.onDestroy()
    life.cancel()
  }

  private fun displayLoginOrPass() {
    displayLogin()
    tryAutoLogin()
  }

  private fun displayLogin() {
    setContentView(R.layout.layout_login)
    btn_login.onClick { loginByUI() }
    et_password.onKey { _, keyCode, event ->
      if (event?.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
        loginByUI()
      }
    }
    loggingIn?.also {
      launch(it) { rotateLoading() }
    }
  }

  private fun tryAutoLogin() {
    val id = app.login?.also {
      et_login.setText(it)
    }
    val pw = app.password?.also {
      et_password.setText(it)
    }
    if (id != null && pw != null) {
      launch(UI) { loginSolely(id, pw) }
    }
  }

  private suspend fun loginByUI() {
    hideKeyboard()

    val id = et_login.text.toString()
    val pass = et_password.text.toString()
    loginSolely(id, pass)
  }

  private fun hideKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
  }

  private suspend fun loginSolely(id: String, pass: String) {
    loginMutex.withLock {
      loggingIn?.cancelAndJoin()
      loggingIn = coroutineContext[Job]
    }

    // If login fails, this child also will be cancelled.
    launch { rotateLoading() }
    login(id, pass)
  }

  private suspend fun login(id: String, pass: String) {
    try {
      RemoteJava.client.login(id, pass)
      saveAutoLoginInfo(id, pass)

      val intent = Intent(this@LoginActivity, MainActivity::class.java)
      startActivity(intent)
      finish()
    } catch (e: Throwable) {
      toast(e.toString())
    }
  }

  private suspend fun rotateLoading() {
    if (iv_loading == null) {
      return
    }
    try {
      iv_loading.visibility = View.VISIBLE
      while (true) {
        iv_loading.rotation += 17f
        delay(1000 / 30)
      }
    } finally {
      iv_loading.visibility = View.GONE
    }
  }

  private fun saveAutoLoginInfo(id: String, pass: String) {
    val autologin = cb_auto_login.isChecked
    val rememberId = cb_remember_id.isChecked
    app.login = if (autologin || rememberId) id else null
    app.password = if (autologin) pass else null
  }
}
