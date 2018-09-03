package kr.ac.kw.coms.globealbum.provider

import android.util.Log
import kotlinx.coroutines.experimental.Job
import kr.ac.kw.coms.landmarks.client.PictureRep
import kr.ac.kw.coms.landmarks.client.Remote
import kr.ac.kw.coms.landmarks.client.ReverseGeocodeResult
import java.io.File

class RemoteJava {

  private val client = Remote()

  fun reverseGeocode(latitude: Double, longitude: Double, prom: Promise<ReverseGeocodeResult>): Job =
    prom.resolve {
      Log.d("RemoteJava", "before $latitude, $longitude")
      val s = client.reverseGeocode(latitude, longitude)
      Log.d("RemoteJava", "after $latitude, $longitude")
      s
    }

  fun checkAlive(prom: Promise<Boolean>): Job =
    prom.resolve { client.checkAlive() }

  fun register(ident: String, pass: String, email: String, nick: String, prom: Promise<Unit>): Job =
    prom.resolve { client.register(ident, pass, email, nick) }

  fun login(ident: String, pass: String, prom: Promise<Unit>): Job =
    prom.resolve { client.login(ident, pass) }

  fun uploadPicture(file: File, latitude: Float? = null, longitude: Float? = null, addr: String? = null, prom: Promise<Unit>): Job =
    prom.resolve { client.uploadPicture(file, latitude, longitude, addr) }

  fun getRandomProblem(prom: Promise<PictureRep>) {
    prom.resolve { client.getRandomProblem() }
  }
}