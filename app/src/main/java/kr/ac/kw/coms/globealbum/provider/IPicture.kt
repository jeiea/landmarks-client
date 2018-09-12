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
import java.io.File
import java.io.InputStream
import java.net.URL
import java.util.*

/**
 * 보편적으로 사진을 다룰 때 쓸 클래스
 * @see kr.ac.kw.coms.globealbum.provider.ResourcePicture
 * @see kr.ac.kw.coms.globealbum.provider.LocalPicture
 * @see kr.ac.kw.coms.globealbum.provider.UrlPicture
 */
abstract class IPicture() : Parcelable {
  /**
   * 비트맵
   */
  open fun drawable(resources: Resources, promise: Promise<Drawable>): Job {
    return promise.resolve {
      BitmapDrawable(resources, stream())
    }
  }

  /**
   * 바이너리 데이터
   */
  open fun stream(promise: Promise<InputStream>): Job {
    return promise.resolve {
      stream()
    }
  }

  /**
   * 바이너리 데이터
   */
  abstract suspend fun stream(): InputStream

  /**
   * 제목
   */
  open var title: String? = null

  /**
   * 생성시각
   */
  open var time: Date? = null

  /**
   * 위치
   */
  open var latlon: Pair<Double, Double>? = null

  /**
   * 사진 삭제
   */
  abstract fun delete()

  /**
   * 변경 사항 저장
   */
  abstract fun save()

  /**
   * Glide에서 모델로 삼기위한 해시로 사용
   */
  abstract override fun toString(): String

  /**
   * Glide에서 캐시 방식을 결정하는데 도움
   */
  open val dataSource: DataSource = DataSource.LOCAL
}

class RemotePicture(val id: Int) : IPicture() {
  override fun toString(): String {
    return "lmserver://picture/$id"
  }

  override val dataSource = DataSource.REMOTE

  constructor(parcel: Parcel) : this(parcel.readInt()) {
    parcel.apply {
      title = readString()
      time = readLong().takeIf { it != -1L }?.let { Date(it) }
      val lat: Double = readDouble()
      val lon: Double = readDouble()
      if (lat != 999.0 && lon != 999.0) {
        latlon = Pair(lat, lon)
      }
    }
  }

  override suspend fun stream(): InputStream {
    return RemoteJava.client.getPicture(id)
  }

  override fun delete() {
    TODO("not implemented")
  }

  override fun save() {
    TODO("not implemented")
  }

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.apply {
      writeInt(id)
      writeString(title)
      writeLong(time?.time ?: -1L)
      writeDouble(latlon?.first ?: 999.0)
      writeDouble(latlon?.second ?: 999.0)
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

class UrlPicture(val url: URL) : IPicture() {
  override fun toString(): String {
    return url.toString()
  }

  override val dataSource = DataSource.REMOTE

  constructor(parcel: Parcel) : this(URL(parcel.readString()))

  override suspend fun stream(): InputStream {
    return url.openStream()
  }

  override fun delete() {
    throw NotImplementedError()
  }

  override fun save() {
    throw NotImplementedError()
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

class LocalPicture(val path: String) : IPicture() {

  constructor(parcel: Parcel) : this(parcel.readString())

  override fun toString() = path

  override suspend fun stream(): InputStream {
    return File(path).inputStream()
  }

  override fun delete() {
    File(path).delete()
  }

  override fun save() {
    TODO("not implemented")
  }

  override var title: String?
    get() = File(path).nameWithoutExtension
    set(value) {}

  override var time: Date?
    get() = Date(EXIFinfo(path).timeTaken);
    set(value) {}

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

class ResourcePicture(@DrawableRes val id: Int) : IPicture() {

  constructor(parcel: Parcel) : this(parcel.readInt())

  override fun toString(): String = "resource:$id"

  override var title: String?
    get() = toString()
    set(_) {}

  override var time: Date?
    get() = Date(1000000L + 1000 * (100 - id))
    set(_) {}

  override suspend fun stream(): InputStream {
    throw NotImplementedError()
  }

  override fun delete() {
    throw NotImplementedError()
  }

  override fun save() {
    throw NotImplementedError()
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

// android.resource://kr.ac.kw.coms.globealbum/drawable/sample2
fun instantiatePicture(context: Context, uri: String): IPicture {
  when {
    uri.startsWith("android.resource:") ->
      return ResourcePicture(uriToResourceId(context, uri))
    uri.startsWith("http") ->
      return UrlPicture(URL(uri))
    uri.startsWith("lmserver://picture/") ->
      return RemotePicture(uri.split('/')[3].toInt())
    else -> throw RuntimeException("")
  }
}

fun resourceToUri(context: Context, resId: Int): Uri {
  return resourceToUri(context.resources, resId)
}

fun resourceToUri(resources: Resources, resId: Int): Uri {
  return Uri.parse(
    ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
      resources.getResourcePackageName(resId) + '/' +
      resources.getResourceTypeName(resId) + '/' +
      resources.getResourceEntryName(resId));
}

fun uriToResourceId(context: Context, uri: String): Int {
  return uriToResourceId(context.resources, uri)
}

fun uriToResourceId(resources: Resources, uri: String): Int {
  val segments: List<String> = uri.split('/')
  return resources.getIdentifier(segments[4], segments[3], segments[2])
}
