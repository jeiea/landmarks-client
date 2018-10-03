package kr.ac.kw.coms.globealbum.provider

import android.util.Log
import kotlinx.coroutines.experimental.Job
import kr.ac.kw.coms.landmarks.client.*
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

  fun getPicture(id: Int, prom: Promise<IPicture>): Job =
    prom.resolve { RemotePicture(WithIntId(id, client.getPictureInfo(id))) }

  fun getRandomPictures(n: Int, promise: Promise<List<RemotePicture>>): Job =
    promise.resolve { client.getRandomProblems(n).map(::RemotePicture) }

  fun modifyPictureInfo(id: Int, info: PictureRep, prom: Promise<Unit>): Job =
    prom.resolve { client.modifyPictureInfo(id, info) }

  fun getMyPictures(prom: Promise<List<IPicture>>): Job =
    prom.resolve { client.getMyPictureInfos().map(::RemotePicture) }

  fun uploadCollection(info: CollectionRep, prom: Promise<Diary>): Job =
    prom.resolve { Diary(client.uploadCollection(info)) }

  fun getMyCollections(prom: Promise<List<Diary>>): Job =
    prom.resolve { client.getMyCollections().map(::Diary) }

  fun modifyCollection(id: Int, info: CollectionRep, prom: Promise<Diary>): Job =
    prom.resolve { Diary(client.modifyCollection(id, info)) }
}
