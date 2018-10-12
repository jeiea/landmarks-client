package kr.ac.kw.coms.globealbum.provider

import android.util.Log
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.Job
import kr.ac.kw.coms.landmarks.client.*
import java.io.File

object RemoteJava {

  val client = Remote()

  init {
    client.logger = object : RemoteLoggable {
      override fun onRequest(msg: String) {
        Log.d("RemoteJava", "Request $msg")
      }

      override fun onResponseSuccess(msg: String) {
        Log.d("RemoteJava", "Receive $msg")
      }

      override fun onResponseFailure(msg: String) {
        Log.d("RemoteJava", "Error $msg")
      }
    }
  }

  fun <T> resolve(promise: Promise<T>, block: suspend () -> T): Job =
    promise.resolve(GlobalScope) { block() }

  fun reverseGeocode(
    latitude: Double,
    longitude: Double,
    prom: Promise<ReverseGeocodeResult>
  ): Job =
    resolve(prom) { client.reverseGeocode(latitude, longitude) }

  fun checkAlive(prom: Promise<Boolean>): Job =
    resolve(prom) { client.checkAlive() }

  fun register(form: AccountForm, prom: Promise<Unit>): Job =
    resolve(prom) { client.register(form.login!!, form.password!!, form.email!!, form.nick!!) }

  fun login(ident: String, pass: String, prom: Promise<IdAccountForm>): Job =
    resolve(prom) { client.login(ident, pass) }

  fun uploadPicture(info: PictureInfo, file: File, prom: Promise<Unit>): Job =
    resolve(prom) { client.uploadPicture(info, file) }

  fun getPicture(id: Int, prom: Promise<IPicture>): Job =
    resolve(prom) { RemotePicture(IdPictureInfo(id, client.getPictureInfo(id))) }

  fun getRandomPictures(n: Int, prom: Promise<List<RemotePicture>>): Job =
    resolve(prom) { client.getRandomProblems(n).map(::RemotePicture) }

  fun modifyPictureInfo(id: Int, info: PictureInfo, prom: Promise<Unit>): Job =
    resolve(prom) { client.modifyPictureInfo(id, info) }

  fun getMyPictures(prom: Promise<List<IPicture>>): Job =
    resolve(prom) { client.getMyPictureInfos().map(::RemotePicture) }

  fun uploadCollection(info: ICollectionInfo, prom: Promise<Diary>): Job =
    resolve(prom) { Diary(client.uploadCollection(info)) }

  fun getMyCollections(prom: Promise<List<Diary>>): Job =
    resolve(prom) { client.getMyCollections().map(::Diary) }

  fun modifyCollection(id: Int, info: ICollectionInfo, prom: Promise<Diary>): Job =
    resolve(prom) { Diary(client.modifyCollection(id, info)) }
}
