package kr.ac.kw.coms.globealbum.provider

import android.util.Log
import kotlinx.coroutines.experimental.Job
import kr.ac.kw.coms.landmarks.client.LoginRep
import kr.ac.kw.coms.landmarks.client.PictureRep
import kr.ac.kw.coms.landmarks.client.Remote
import kr.ac.kw.coms.landmarks.client.ReverseGeocodeResult
import org.osmdroid.util.GeoPoint
import java.io.File

public object RemoteJava {

  val client = Remote()

  fun reverseGeocode(latitude: Double, longitude: Double, prom: Promise<ReverseGeocodeResult>): Job =
    prom.resolve {
      Log.d("RemoteJava", "before $latitude, $longitude")
      val s = client.reverseGeocode(latitude, longitude)
      Log.d("RemoteJava", "after $latitude, $longitude")
      s
    }

  fun checkAlive(prom: Promise<Boolean>): Job =
    prom.resolve { client.checkAlive() }

  fun register(form: LoginRep, prom: Promise<Unit>): Job =
    prom.resolve { client.register(form.login!!, form.password!!, form.email!!, form.nick!!) }

  fun login(ident: String, pass: String, prom: Promise<Unit>): Job =
    prom.resolve { client.login(ident, pass) }

  fun uploadPicture(info: PictureRep, file: File, prom: Promise<Unit>): Job =
    prom.resolve { client.uploadPicture(info, file) }

  fun getRandomPictures(n: Int, promise: Promise<List<RemotePicture>>) {
    promise.resolve {
      client.getRandomProblems(n).map { pic ->
        val p = pic.value
        RemotePicture(pic.id).apply {
          val (lat, lon) = Pair(p.lat, p.lon)
          if (lat != null && lon != null) {
            geo = GeoPoint(lat.toDouble(), lon.toDouble())
          }
          time = p.time
          title = p.address
        }
      }
    }
  }
}
