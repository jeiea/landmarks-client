package kr.ac.kw.coms.globealbum.map

import kr.ac.kw.coms.globealbum.provider.IPicture
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView

/**
 * 맵뷰에서 구현해야 할 것들
 */
interface ILandmarkMapView {
  /**
   * 가급적 쓰지 말 것. 내부 사용 용도.
   */
  val mapView: MapView
  /**
   * 다이어리 전용 인터페이스
   */
  val diaryOverlay: IDiaryOverlay
}

/**
 * 동그라미 섬네일을 다루는 인터페이스
 */
interface IDiaryOverlay {
  /**
   * 선 연결이 필요하지 않은 사진 묶음
   */
  var groups: List<List<IPicture>>

  /**
   * 선 연결이 필요한 사진 묶음
   */
  var chains: List<List<IPicture>>

  /**
   * 터치 이벤트 리스너
   */
  var onThumbnailClick: ((IPicture) -> Boolean)?

  /**
   * 선택 효과 내기
   */
  fun addToSelection(picture: IPicture)

  /**
   * 선택 효과 지우기
   */
  fun removeFromSelection(picture: IPicture)

  /**
   * 선택 효과 지우기
   */
  fun clearSelection()

  /**
   * 섬네일 지역 가져오기
   */
  fun getBoundingBox(): BoundingBox
}
