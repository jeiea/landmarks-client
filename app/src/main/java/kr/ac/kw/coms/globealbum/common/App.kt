package kr.ac.kw.coms.globealbum.common

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

class App : Application() {

  override fun onCreate() {
    super.onCreate()
    app = this
  }

  val credContainer: SharedPreferences
    get() = getSharedPreferences("Credential", Context.MODE_PRIVATE)

  var login: String?
    get() = credContainer.getString("id", null)
    set(value) = credContainer.edit().putString("id", value).apply()

  var password: String?
    get() = credContainer.getString("pw", null)
    set(value) = credContainer.edit().putString("pw", value).apply()
}

lateinit var app: App
  private set
