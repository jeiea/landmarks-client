package kr.ac.kw.coms.globealbum.provider

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.util.Pair
import com.bumptech.glide.RequestBuilder
import kr.ac.kw.coms.globealbum.common.GlideApp
import java.io.File
import java.util.*

/**
 * 보편적으로 사진을 다룰 때 쓸 클래스
 * @see kr.ac.kw.coms.globealbum.provider.ResourcePicture
 * @see kr.ac.kw.coms.globealbum.provider.LocalPicture
 * @see kr.ac.kw.coms.globealbum.provider.UriPicture
 */
interface IPicture {
  /**
   * 비트맵
   */
  val drawable: RequestBuilder<Drawable>

  /**
   * 제목
   */
  var title: String

  /**
   * 생성시각
   */
  val time: Date

  /**
   * 위치
   */
  val coords: Pair<Double, Double>

  /**
   * 사진 삭제
   */
  fun delete()
}

class UriPicture(val uri: android.net.Uri, val context: Context) : IPicture {

  override val drawable: RequestBuilder<Drawable>
    get() = GlideApp.with(context).load(uri)

  override var title: String
    get() = uri.lastPathSegment
    set(value) = throw NotImplementedError()

  override val time: Date
    get() = throw NotImplementedError()

  override val coords: Pair<Double, Double>
    get() = throw NotImplementedError()

  override fun delete() = throw NotImplementedError()
}

class LocalPicture(val path: String, val context: Context) : IPicture {

  override val drawable: RequestBuilder<Drawable>
    get() = GlideApp.with(context).load(File(path))

  override var title: String
    get() = File(path).nameWithoutExtension
    set(value) {}

  override val time: Date
    get() = Date(EXIFinfo(path).timeTaken);

  override val coords: Pair<Double, Double>
    get() = TODO("not implemented")

  override fun delete() = throw NotImplementedError()
}

class ResourcePicture(val context: Context, @DrawableRes val id: Int) : IPicture {

  override val drawable: RequestBuilder<Drawable>
    get() = GlideApp.with(context).load(id)

  override var title: String
    get() = "resource:$id"
    set(value) {}

  override val time: Date
    get() = Date(1000000L + 1000 * (100 - id))

  override val coords: Pair<Double, Double>
    get() = throw NotImplementedError()

  override fun delete() = throw NotImplementedError()
}