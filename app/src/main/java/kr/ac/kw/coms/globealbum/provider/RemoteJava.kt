package kr.ac.kw.coms.globealbum.provider

import android.util.Log
import kotlinx.coroutines.experimental.*
import kr.ac.kw.coms.landmarks.client.*
import java.io.File
import java.util.*

object RemoteJava {

  val client = Remote()

  init {
    client.logger = object : RemoteLoggable {
      override fun onRequest(msg: String) {
        Log.d("RemoteJava", "${Date()} Request $msg")
      }

      override fun onResponseSuccess(msg: String) {
        Log.d("RemoteJava", "${Date()} Receive $msg")
      }

      override fun onResponseFailure(msg: String) {
        Log.d("RemoteJava", "${Date()} Error $msg")
      }
    }
  }

  private fun <T> resolve(promise: Promise<T>, block: suspend () -> T): Job =
    promise.resolve(GlobalScope + Dispatchers.IO) { block() }

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

  fun uploadPicture(info: PictureInfo, file: File, prom: Promise<RemotePicture>): Job =
    resolve(prom) { RemotePicture(client.uploadPicture(info, file)) }

  fun uploadPicture(local: LocalPicture, prom: Promise<RemotePicture>): Job = resolve(prom) {
    uploadPicture(local)
  }

  private suspend fun uploadPicture(local: LocalPicture): RemotePicture {
    val gps = EXIFinfo(local.path).locationGeopoint ?: throw RuntimeException("GPS 정보 없음")
    val res = client.reverseGeocode(gps.latitude, gps.longitude)
    val info = PictureInfo(address = "${res.country} ${res.detail}")
    val file = File(local.path)
    return RemotePicture(client.uploadPicture(info, file))
  }

  fun getPicture(id: Int, prom: Promise<RemotePicture>): Job =
    resolve(prom) { RemotePicture(IdPictureInfo(id, client.getPictureInfo(id))) }

  fun getRandomPictures(n: Int, prom: Promise<List<RemotePicture>>): Job =
    resolve(prom) { client.getRandomPictures(n).map(::RemotePicture) }

  fun modifyPictureInfo(id: Int, info: PictureInfo, prom: Promise<Unit>): Job =
    resolve(prom) { client.modifyPictureInfo(id, info) }

  fun getPictures(query: PictureQuery?, prom: Promise<List<RemotePicture>>): Job = resolve(prom) {
    client.getPictures(query).map(::RemotePicture)
  }

  fun getMyPictures(prom: Promise<List<RemotePicture>>): Job = resolve(prom) {
    client.getPictures(PictureQuery().apply {
      limit = 999999
      userFilter = UserFilter.Include(client.profile!!.id)
    }).map(::RemotePicture)
  }

  fun getPicturesAround(
    lat: Double, lon: Double, km: Double, prom: Promise<List<RemotePicture>>
  ): Job = resolve(prom) {
    client.getPictures(PictureQuery().apply {
      geoFilter = NearGeoPoint(lat, lon, km)
    }).map(::RemotePicture)
  }

  fun deletePicture(id: Int, prom: Promise<Unit>): Job =
    resolve(prom) { client.deletePicture(id) }

  fun uploadCollection(diary: Diary, prom: Promise<Diary>): Job = resolve(prom) {
    uploadLocalPictures(diary) { client.uploadCollection(it.info.data) }
  }

  private suspend fun uploadLocalPictures(
    diary: Diary,
    block: suspend (Diary) -> IdCollectionInfo
  ): Diary = coroutineScope {

    val pics = diary.pictures.map {
      async { if (it is LocalPicture) uploadPicture(it) else it }
    }.awaitAll()
    diary.pictures = pics

    Diary(block(diary))
  }

  fun getMyCollections(prom: Promise<List<Diary>>): Job = resolve(prom) {
    client.getMyCollections().map { c ->
      val pics = c.data.previews?.map(::RemotePicture) ?: listOf()
      Diary(c, pics)
    }
  }

  fun getRandomCollections(prom: Promise<List<Diary>>): Job = resolve(prom) {
    client.getRandomCollections().map { c ->
      val pics = c.data.previews?.map(::RemotePicture) ?: listOf()
      Diary(c, pics)
    }
  }

  fun getCollectionsContainPicture(picId: Int, prom: Promise<MutableList<IdCollectionInfo>>): Job =
    resolve(prom) { client.getCollectionsContainPicture(picId) }

  fun modifyCollection(diary: Diary, prom: Promise<Diary>): Job = resolve(prom) {
    uploadLocalPictures(diary) { client.modifyCollection(diary.id, diary.info.data) }
  }

  fun deleteCollection(id: Int, prom: Promise<Unit>): Job =
    resolve(prom) { client.deleteCollection(id) }
}
