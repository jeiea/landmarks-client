package kr.ac.kw.coms.globealbum.provider

import kr.ac.kw.coms.landmarks.client.PictureRep
import kr.ac.kw.coms.landmarks.client.Promise
import kr.ac.kw.coms.landmarks.client.Remote
import java.io.File

class RemoteJava {

  private val client = Remote()

  fun reverseGeocode(latitude: Double, longitude: Double, prom: Promise<Pair<String?, String?>?>) {
    prom.resolve { client.reverseGeocode(latitude, longitude) }
  }

  fun checkAlive(prom: Promise<Boolean>) {
    prom.resolve { client.checkAlive() }
  }

  fun register(ident: String, pass: String, email: String, nick: String, prom: Promise<Unit>) {
    prom.resolve { client.register(ident, pass, email, nick) }
  }

  fun login(ident: String, pass: String, prom: Promise<Unit>) {
    prom.resolve { client.login(ident, pass) }
  }

  fun uploadPicture(file: File, latitude: Float? = null, longitude: Float? = null, addr: String? = null, prom: Promise<Unit>) {
    prom.resolve { client.uploadPicture(file, latitude, longitude, addr) }
  }

  fun getRandomProblem(prom: Promise<PictureRep>) {
    TODO()
//    prom.resolve { }
  }
}