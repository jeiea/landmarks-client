package kr.ac.kw.coms.globealbum.provider

import android.graphics.drawable.Drawable
import android.util.Pair
import com.bumptech.glide.RequestBuilder
import java.util.*

/**
 * 보편적으로 사진을 다룰 때 쓸 클래스
 * @see kr.ac.kw.coms.globealbum.album.ResourcePicture
 * @see kr.ac.kw.coms.globealbum.album.LocalPicture
 * @see kr.ac.kw.coms.globealbum.album.UriPicture
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
