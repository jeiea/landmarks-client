package kr.ac.kw.coms.globealbum.provider

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import com.bumptech.glide.load.DataSource
import kotlinx.coroutines.experimental.Job
import kr.ac.kw.coms.landmarks.client.Remote
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
abstract class IPicture {
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

class RemotePicture(val client: Remote, val id: Int) : IPicture() {
  override fun toString(): String {
    return "${client.basePath}/picture/$id"
  }

  override val dataSource = DataSource.REMOTE

  override suspend fun stream(): InputStream {
    return client.getPicture(id)
  }

  override fun delete() {
    TODO("not implemented")
  }

  override fun save() {
    TODO("not implemented")
  }
}

class UrlPicture(val url: URL) : IPicture() {
  override fun toString(): String {
    return url.toString()
  }

  override val dataSource = DataSource.REMOTE

  override suspend fun stream(): InputStream {
    return url.openStream()
  }

  override fun delete() {
    throw NotImplementedError()
  }

  override fun save() {
    throw NotImplementedError()
  }
}

class LocalPicture(val path: String, val context: Context) : IPicture() {
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
}

class ResourcePicture(val context: Context, @DrawableRes val id: Int) : IPicture() {

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
}