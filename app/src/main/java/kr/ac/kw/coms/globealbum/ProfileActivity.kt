package kr.ac.kw.coms.globealbum

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.coroutineScope
import kotlinx.coroutines.experimental.launch
import kr.ac.kw.coms.globealbum.common.GlideApp
import kr.ac.kw.coms.globealbum.common.app
import kr.ac.kw.coms.globealbum.provider.LoginActivity
import org.jetbrains.anko.sdk27.coroutines.onClick

class ProfileActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_profile)
    profile_logout_button.onClick {
      app.password = null
      startActivity(Intent(this@ProfileActivity, LoginActivity::class.java))
      finish()
    }
    profile_back_textview.onClick {
      coroutineScope {
        launch(Dispatchers.IO) {
          GlideApp.get(applicationContext).clearDiskCache()
        }
        GlideApp.get(applicationContext).clearMemory()
      }
      finish()
    }
  }
}
