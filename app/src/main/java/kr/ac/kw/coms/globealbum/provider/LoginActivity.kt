package kr.ac.kw.coms.globealbum.provider

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import kotlinx.android.synthetic.main.layout_login.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kr.ac.kw.coms.globealbum.MainActivity
import kr.ac.kw.coms.globealbum.R
import kr.ac.kw.coms.globealbum.common.app
import org.jetbrains.anko.contentView
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onKey
import org.jetbrains.anko.toast


class LoginActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.layout_start_loading)
  }

  override fun onResume() {
    super.onResume()
    contentView?.postDelayed(::displayLoginOrPass, 1000)
  }

  private fun displayLoginOrPass() {
    displayLogin()
    tryAutoLogin()
  }

  private fun displayLogin() {
    setContentView(R.layout.layout_login)
    btn_login.onClick { tryLoginByUI() }
    et_password.onKey { _, keyCode, event ->
      if (event?.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
        tryLoginByUI()
      }
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
      launch(UI) { tryLogin(id, pw) }
    }
  }

  private suspend fun tryLoginByUI() {
    hideKeyboard()

    val id = et_login.text.toString()
    val pass = et_password.text.toString()
    tryLogin(id, pass)
  }

  private fun hideKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
  }

  private suspend fun tryLogin(id: String, pass: String) {
    val animation = rotateLoading()
    try {
      RemoteJava.client.login(id, pass)
      saveAutoLoginInfo(id, pass)

      val intent = Intent(this@LoginActivity, MainActivity::class.java)
      startActivity(intent)
    } catch (e: Throwable) {
      animation.cancel()
      toast(e.toString())
    }
  }

  private suspend fun rotateLoading() = launch(UI) {
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
