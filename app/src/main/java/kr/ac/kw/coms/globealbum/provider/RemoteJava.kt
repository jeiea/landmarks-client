package kr.ac.kw.coms.globealbum.provider

import android.content.Context
import android.util.Log
import com.bumptech.glide.Glide
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.plus
import kr.ac.kw.coms.landmarks.client.*
import java.io.File
import java.util.*

object RemoteJava {

  val client = Remote()
  var picBuffer: ReceiveChannel<RemotePicture>? = null
  fun picBuffering(view: Context) = GlobalScope.produce(Dispatchers.Main) {
    while (true) {
      val p = client.getRandomProblems(1)[0]
      val pic = RemotePicture(p)
      Glide.with(view).download(pic).preload()
      send(pic)
    }
  }

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

  fun <T> resolve(promise: Promise<T>, block: suspend () -> T): Job =
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

  fun uploadPicture(info: PictureInfo, file: File, prom: Promise<Unit>): Job =
    resolve(prom) { client.uploadPicture(info, file) }

  fun getPicture(id: Int, prom: Promise<IPicture>): Job =
    resolve(prom) { RemotePicture(IdPictureInfo(id, client.getPictureInfo(id))) }

  fun getRandomPictures(
    n: Int, context: Context, prom: Promise<List<RemotePicture>>
  ): Job = resolve(prom) {
    val buf = (picBuffer ?: {
      val b = picBuffering(context)
      picBuffer = b
      b
    }())
    val ret = mutableListOf<RemotePicture>()
    repeat(n) { ret.add(buf.receive()) }
    ret
  }

  fun modifyPictureInfo(id: Int, info: PictureInfo, prom: Promise<Unit>): Job =
    resolve(prom) { client.modifyPictureInfo(id, info) }

  fun getMyPictures(prom: Promise<List<IPicture>>): Job =
    resolve(prom) { client.getMyPictureInfos().map(::RemotePicture) }

  fun getAroundPictures(
    lat: Double, lon: Double, km: Double, prom: Promise<MutableList<IdPictureInfo>>
  ): Job =
    resolve(prom) { client.getAroundPictures(lat, lon, km) }

  fun uploadCollection(info: ICollectionInfo, prom: Promise<Diary>): Job =
    resolve(prom) { Diary(client.uploadCollection(info)) }

  fun getMyCollections(prom: Promise<List<Diary>>): Job =
    resolve(prom) { client.getMyCollections().map(::Diary) }

  fun getCollectionsContainPicture(picId: Int, prom: Promise<MutableList<IdCollectionInfo>>): Job =
    resolve(prom) { client.getCollectionsContainPicture(picId) }

  fun modifyCollection(id: Int, info: ICollectionInfo, prom: Promise<Diary>): Job =
    resolve(prom) { Diary(client.modifyCollection(id, info)) }

}
