package kr.ac.kw.coms.globealbum.provider

import awaitStringResult
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.FormBody
import okhttp3.Request

class LandmarksClient {

  private val authUrl = "https://coms-globe.herokuapp.com/auth/page/email"
  val gson = Gson()

  suspend fun register(email: String): String {
    val obj = JsonObject()
    obj.addProperty("email", email)
    return Fuel.post(authUrl + "/register")
      .header("Accept" to "application/json")
      .header("Content-Type" to "application/json")
      .header("User-Agent" to "landmarks-client")
      .body(gson.toJson(obj))
      .awaitStringResult()
  }

  fun login(url: String, id: String, password: String) {
    val requestBody = FormBody.Builder().add("userId", id).add("userPassword", password).build()

    val request = Request.Builder().url(url).post(requestBody).build()
  }
}