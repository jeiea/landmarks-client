package kr.ac.kw.coms.globealbum.provider

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.layout_login.*
import kr.ac.kw.coms.globealbum.MainActivity
import kr.ac.kw.coms.globealbum.R
import org.jetbrains.anko.contentView
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.toast

class LoginActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.layout_start_loading)
  }

  override fun onResume() {
    super.onResume()
    contentView?.postDelayed(::displayLogin, 1000)
  }

  private fun displayLogin() {
    if (et_login == null) {
      setContentView(R.layout.layout_login)
      btn_login.onClick { onLogin() }
    }
  }

  suspend fun onLogin() {
    val id = et_login.text.toString()
    val pass = et_password.text.toString()
//      RemoteJava.client.login(id, pass)
    try {
      RemoteJava.client.login("login", "password")
      val intent = Intent(this@LoginActivity, MainActivity::class.java)
      startActivity(intent)
    } catch (e: Throwable) {
      toast(e.toString())
    }
  }
}
