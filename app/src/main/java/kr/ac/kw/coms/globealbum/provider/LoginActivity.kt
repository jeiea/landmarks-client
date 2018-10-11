package kr.ac.kw.coms.globealbum.provider

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.layout_login.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kr.ac.kw.coms.globealbum.MainActivity
import kr.ac.kw.coms.globealbum.R
import kr.ac.kw.coms.globealbum.common.app
import org.jetbrains.anko.contentView
import org.jetbrains.anko.sdk27.coroutines.onClick
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
    val id = app.login?.also {
      et_login.setText(it)
    }
    val pw = app.password?.also {
      et_password.setText(it)
    }
    if (id != null && pw != null) {
      launch(UI) { tryLogin(id, pw) }
    } else {
      setContentView(R.layout.layout_login)
      btn_login.onClick { onLogin() }
    }
  }

  suspend fun onLogin() {
    val id = et_login.text.toString()
    val pass = et_password.text.toString()
    tryLogin(id, pass)
  }

  private suspend fun tryLogin(id: String, pass: String) {
    try {
      RemoteJava.client.login(id, pass)
      saveAutoLoginInfo(id, pass)

      val intent = Intent(this@LoginActivity, MainActivity::class.java)
      startActivity(intent)
    } catch (e: Throwable) {
      toast(e.toString())
    }
  }

  private fun saveAutoLoginInfo(id: String, pass: String) {
    val autologin = cb_auto_login.isChecked
    val rememberId = cb_remember_id.isChecked
    app.login = if (autologin || rememberId) id else null
    app.password = if (autologin) pass else null
  }
}
