package kr.ac.kw.coms.globealbum

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View

class SignUpActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.fragment_sign_up)
  }

  fun OnClickBack(_v: View) {
    finish()
  }
}
