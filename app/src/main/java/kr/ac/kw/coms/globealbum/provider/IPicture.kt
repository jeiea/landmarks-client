package kr.ac.kw.coms.globealbum.provider

import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.DrawableRes
import com.bumptech.glide.load.DataSource
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.runBlocking
import kr.ac.kw.coms.landmarks.client.*
import org.osmdroid.util.GeoPoint
import java.io.File
import java.io.InputStream
import java.net.URL
import java.util.*

interface Deletable {
  /**
   * 삭제
   */
  fun delete()
}

data class PictureMeta(
  /**
   * 제목
   */
  var address: String? = null,
  /**
   * 저작자
   */
  var author: String? = null,
  /**
   * 생성시각
   */
  var time: Date? = null,
  /**
   * 위치
   */
  var geo: GeoPoint? = null
) : Parcelable {

  constructor(parcel: Parcel) : this(
    parcel.readString(),
    parcel.readString(),
    if (parcel.readInt() == 1) Date(parcel.readLong()) else null,
    parcel.readParcelable(GeoPoint::class.java.classLoader)
  )

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeString(address)
    parcel.writeString(author)
    val t = time
    when (t) {
      null -> parcel.writeInt(0)
      else -> {
        parcel.writeInt(1)
        parcel.writeLong(t.time)
      }
    }
    parcel.writeParcelable(geo, flags)
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Parcelable.Creator<PictureMeta> {
    override fun createFromParcel(parcel: Parcel): PictureMeta {
      return PictureMeta(parcel)
    }

    override fun newArray(size: Int): Array<PictureMeta?> {
      return arrayOfNulls(size)
    }
  }
}

/**
 * 보편적으로 사진을 다룰 때 쓰는 인터페이스
 * @see kr.ac.kw.coms.globealbum.provider.ResourcePicture
 * @see kr.ac.kw.coms.globealbum.provider.LocalPicture
 * @see kr.ac.kw.coms.globealbum.provider.UrlPicture
 */
interface IPicture : Parcelable {
  /**
   * 메타 데이터
   */
  var meta: PictureMeta

  /**
   * 비트맵
   */
  fun drawable(resources: Resources, promise: Promise<Drawable>): Job {
    return runBlocking {
      promise.resolve {
        BitmapDrawable(resources, stream())
      }
    }
  }

  /**
   * 바이너리 데이터
   */
  fun stream(promise: Promise<InputStream>): Job {
    return runBlocking {
      promise.resolve { stream() }
    }
  }

  /**
   * 바이너리 데이터
   */
  suspend fun stream(): InputStream

  /**
   * Glide에서 모델로 삼기위한 해시로 사용
   */
  override fun toString(): String

  /**
   * Glide에서 캐시 방식을 결정하는데 도움
   */
  val dataSource: DataSource
}

class RemotePicture(val info: IdPictureInfo) :
  IPicture, Deletable, IPictureInfo by info.data {

  fun latlonToGeoPoint(pic: PictureInfo): GeoPoint? {
    return pic.let { v ->
      val lat = v.lat ?: return null
      val lon = v.lon ?: return null
      GeoPoint(lat, lon)
    }
  }

  // TODO: Update picture metadata
  override var meta = PictureMeta(
    info.data.address,
    info.data.author,
    info.data.time,
    latlonToGeoPoint(info.data)
  )

  override val dataSource = DataSource.REMOTE

  override fun toString(): String {
    return "lmserver://picture/$info"
  }

  constructor(parcel: Parcel) : this(IdPictureInfo(parcel.readInt(), PictureInfo())) {
    parcel.apply {
      val v = info.data
      v.uid = readInt().takeIf { it != -1 }
      v.author = readString()
      v.address = readString()
      v.lat = readDouble().takeIf { it != 999.0 }
      v.lon = readDouble().takeIf { it != 999.0 }
      v.time = readLong().takeIf { it != -1L }?.let { Date(it) }
      v.isPublic = readByte() == 1.toByte()
    }
  }

  override suspend fun stream(): InputStream {
    return RemoteJava.client.getPicture(info.id)
  }

  override fun delete() {
    TODO("not implemented")
  }

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.apply {
      writeInt(info.id)
      val v = info.data
      writeInt(v.uid ?: -1)
      writeString(v.author)
      writeString(v.address)
      writeDouble(v.lat ?: 999.0)
      writeDouble(v.lon ?: 999.0)
      writeLong(v.time?.time ?: -1L)
      writeByte(if (v.isPublic) 1 else 0)
    }
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Parcelable.Creator<RemotePicture> {
    override fun createFromParcel(parcel: Parcel): RemotePicture {
      return RemotePicture(parcel)
    }

    override fun newArray(size: Int): Array<RemotePicture?> {
      return arrayOfNulls(size)
    }
  }
}

class UrlPicture(val url: URL) : IPicture {
  override var meta: PictureMeta
    get() {
      return PictureMeta(url.toString(), url.authority, null, null)
    }
    set(_) {}

  override fun toString(): String {
    return url.toString()
  }

  override val dataSource = DataSource.REMOTE

  constructor(parcel: Parcel) : this(URL(parcel.readString()))

  override suspend fun stream(): InputStream {
    return url.openStream()
  }

  //region Parcelable implementation

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeString(url.toString())
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Parcelable.Creator<UrlPicture> {
    override fun createFromParcel(parcel: Parcel): UrlPicture {
      return UrlPicture(parcel)
    }

    override fun newArray(size: Int): Array<UrlPicture?> {
      return arrayOfNulls(size)
    }
  }

  //endregion
}

class LocalPicture(val path: String) : IPicture, Deletable {
  override var meta: PictureMeta
    get() {
      val filename = File(path).nameWithoutExtension
      val metadata = EXIFinfo(path)
      val time = Date(metadata.timeTaken)
      val geo = metadata.locationGeopoint
      return PictureMeta(filename, "You", time, geo)
    }
    set(_) {}

  override val dataSource = DataSource.LOCAL

  constructor(parcel: Parcel) : this(parcel.readString())

  override fun toString() = path

  override suspend fun stream(): InputStream {
    return File(path).inputStream()
  }

  override fun delete() {
    File(path).delete()
  }

  //region Parcelable implementation

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeString(path)
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Parcelable.Creator<LocalPicture> {
    override fun createFromParcel(parcel: Parcel): LocalPicture {
      return LocalPicture(parcel)
    }

    override fun newArray(size: Int): Array<LocalPicture?> {
      return arrayOfNulls(size)
    }
  }

  //endregion
}

class ResourcePicture(@DrawableRes val id: Int) : IPicture {

  override fun toString() = "resource:$id"

  override var meta: PictureMeta
    get() {
      val exif: EXIFinfo = EXIFinfo().apply { setMetadata(resources!!.openRawResource(+id)) }
      val geo = exif.locationGeopoint
      return PictureMeta(toString(), toString(), Date(1000000L + 1000 * (100 - id)), geo)
    }
    set(_) {}

  override val dataSource = DataSource.LOCAL

  var resources: Resources? = null

  constructor(@DrawableRes id: Int, resources: Resources) : this(id) {
    this.resources = resources
  }

  constructor(parcel: Parcel) : this(parcel.readInt())

  override suspend fun stream(): InputStream {
    return resources?.openRawResource(+id) ?: "".byteInputStream()
  }

  //region Parcelable implementation

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeInt(id)
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Parcelable.Creator<ResourcePicture> {
    override fun createFromParcel(parcel: Parcel): ResourcePicture {
      return ResourcePicture(parcel)
    }

    override fun newArray(size: Int): Array<ResourcePicture?> {
      return arrayOfNulls(size)
    }
  }

  //endregion
}

class Diary(
  var info: IdCollectionInfo = IdCollectionInfo(-1, CollectionInfo()),
  var pictures: List<IPicture> = listOf()
) : Parcelable, ICollectionInfo by info.data, IntIdentifiable by info {

  //region Parcelable implementation
  constructor(parcel: Parcel) : this() {
    info.id = parcel.readInt()
    val v = info.data
    parcel.run {
      v.title = readString()
      v.text = readString()
      pictures = generateSequence {
        when (readByte()) {
          1.toByte() -> LocalPicture::class
          2.toByte() -> RemotePicture::class
          else -> null
        }?.let { readParcelable<IPicture>(it.java.classLoader) }
      }.toList()
      v.likes = readInt().takeIf { it != -1 }
      v.liking = byteToBool(readByte())
      v.isRoute = byteToBool(readByte())
      v.parent = readInt().takeIf { it != -1 }
    }
  }

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    val v = info.data
    parcel.run {
      writeInt(info.id)
      writeString(v.title)
      writeString(v.text)
      pictures.forEach { pic ->
        writeByte(
          when (pic) {
            is LocalPicture -> 1.toByte()
            is RemotePicture -> 2.toByte()
            else -> 0.toByte()
          }
        )
        writeParcelable(pic, 0)
      }
      writeInt(v.likes ?: -1)
      writeByte(boolToByte(v.liking))
      writeByte(boolToByte(v.isRoute))
      writeInt(v.parent ?: -1)
    }
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Parcelable.Creator<Diary> {
    override fun createFromParcel(parcel: Parcel) = Diary(parcel)

    override fun newArray(size: Int): Array<Diary?> {
      return arrayOfNulls(size)
    }

    fun boolToByte(b: Boolean?): Byte = when (b) {
      true -> 1
      false -> 0
      null -> -1
    }

    fun byteToBool(b: Byte): Boolean? = when (b) {
      1.toByte() -> true
      0.toByte() -> false
      else -> null
    }
  }
  //endregion
}

fun resourceToUri(context: Context, resId: Int): Uri {
  return resourceToUri(context.resources, resId)
}

fun resourceToUri(resources: Resources, resId: Int): Uri {
  return Uri.parse(
    ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
      resources.getResourcePackageName(resId) + '/' +
      resources.getResourceTypeName(resId) + '/' +
      resources.getResourceEntryName(resId)
  )
}

fun uriToResourceId(context: Context, uri: String): Int {
  return uriToResourceId(context.resources, uri)
}

fun uriToResourceId(resources: Resources, uri: String): Int {
  val segments: List<String> = uri.split('/')
  return resources.getIdentifier(segments[4], segments[3], segments[2])
}
