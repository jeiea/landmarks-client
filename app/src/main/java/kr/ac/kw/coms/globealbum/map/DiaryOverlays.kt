package kr.ac.kw.coms.globealbum.map

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.util.TypedValue
import android.view.MotionEvent
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.launch
import kr.ac.kw.coms.globealbum.common.AsyncTarget
import kr.ac.kw.coms.globealbum.common.Disposable
import kr.ac.kw.coms.globealbum.common.GlideApp
import kr.ac.kw.coms.globealbum.provider.IPicture
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.DefaultOverlayManager
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Polyline
import kotlin.math.sqrt

private const val mmThumbDiameter = 10f

class DiaryOverlayFolder(private val mapView: MapView) : Overlay(), IDiaryOverlay, Disposable {

  private val badgeOffset: Float
  private val px2: Float
  private val fontPaint: Paint

  private var journeyGroups = listOf<Journey>()
  private var journeyChains = listOf<Journey>()

  override var onThumbnailClick: ((IPicture) -> Boolean)? = null
  private fun onThumbnailClick(pic: IPicture): Boolean {
    return onThumbnailClick?.invoke(pic) ?: true
  }

  override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
    for ((_, marker) in allCircleMarkers) {
      if (marker.onSingleTapConfirmed(e, mapView)) {
        return true
      }
    }
    return false
  }

  // Use cache
  override var groups
    get() = journeyGroups.map { g -> g.pictures }
    set(value) {
      val j = journeyGroups
      journeyGroups = value.map {
        Journey(mapView, it, false).apply { onClick = ::onThumbnailClick }
      }
      j.forEach(Disposable::dispose)
    }
  override var chains
    get() = journeyChains.map { g -> g.pictures }
    set(value) {
      val j = journeyChains
      journeyChains = value.map {
        Journey(mapView, it, true).apply { onClick = ::onThumbnailClick }
      }
      j.forEach(Disposable::dispose)
    }

  override fun dispose() {
    allJourneys.forEach(Disposable::dispose)
  }

  init {
    val metrics = mapView.resources.displayMetrics
    val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, mmThumbDiameter, metrics)
    badgeOffset = px * sqrt(2f) / 4
    px2 = px * px
    fontPaint = Paint().apply {
      textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15f, metrics)
      color = (0xffff0000).toInt()
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
  }

  override fun launchReceive(scope: CoroutineScope) {
    allJourneys.forEach { it.launchReceive(scope) }
  }

  val allJourneys get() = journeyGroups.asSequence() + journeyChains

  val allCircleMarkers: Sequence<Pair<Journey, CircleMarker>>
    get() = allJourneys.flatMap { journey ->
      journey.manager.asSequence().filterIsInstance<CircleMarker>().map { Pair(journey, it) }
    }

  private val allMarkersAndPictures
    get() = allJourneys.flatMap {
      it.markers.asSequence().zip(it.pictures.asSequence())
    }

  override fun addToSelection(picture: IPicture) {
    for ((marker, pic) in allMarkersAndPictures) {
      if (pic == picture) {
        marker.setColor(Color.YELLOW)
      }
    }
  }

  override fun removeFromSelection(picture: IPicture) {
    for ((marker, pic) in allMarkersAndPictures) {
      if (pic == picture) {
        marker.setColor(Color.WHITE)
      }
    }
  }

  override fun clearSelection() {
    for ((_, marker) in allCircleMarkers) {
      marker.setColor(Color.WHITE)
    }
  }

  override fun draw(c: Canvas, osmv: MapView, shadow: Boolean) {
    val overlays = (journeyChains + journeyGroups).flatMap { it.manager.overlays() }
    drawRoutes(overlays, c, osmv)
    drawCircles(overlays, c, osmv)
  }

  private fun drawRoutes(overlays: List<Overlay>, c: Canvas, osmv: MapView) {
    val routes = overlays.filterIsInstance<Polyline>()
    routes.forEach { it.draw(c, osmv, false) }
  }

  private fun drawCircles(overlays: List<Overlay>, c: Canvas, osmv: MapView) {
    val circles = overlays.filterIsInstance<CircleMarker>()
    renewMarkerDisplayPositions(osmv, circles)
    drawCirclesWithOverlaps(circles, c, osmv)
  }

  private fun renewMarkerDisplayPositions(osmv: MapView, circles: List<CircleMarker>) {
    val pj = osmv.projection
    circles.forEach { m -> pj.toPixels(m.geo, m.point) }
  }

  private fun drawCirclesWithOverlaps(circles: List<CircleMarker>, c: Canvas, osmv: MapView) {
    val overlaps = hashMapOf<Point, Int>()
    circles.forEach { m ->
      val near = overlaps.keys.firstOrNull { p -> distanceSquare(p, m.point) < px2 }
      if (near == null) {
        m.draw(c, osmv, false)
        overlaps[m.point] = 1
      }
      else {
        overlaps[near] = overlaps[near]!! + 1
      }
    }
    overlaps.entries.filter { it.value > 1 }.forEach { (point, cnt) ->
      c.drawText("$cnt", point.x + badgeOffset, point.y - badgeOffset, fontPaint)
    }
  }

  private fun distanceSquare(p1: Point, p2: Point): Float {
    val xDiff = (p1.x - p2.x).toFloat()
    val yDiff = (p1.y - p2.y).toFloat()
    return xDiff * xDiff + yDiff * yDiff
  }

  override fun getBoundingBox(): BoundingBox {
    val positions = (journeyChains + journeyGroups).map { j ->
      j.pictures.map { it.meta.geo }
    }.flatten().filterNotNull()

    if (positions.isEmpty()) {
      return mapView.boundingBox
    }
    val s = positions.minBy { it.latitude }!!.latitude
    val n = positions.maxBy { it.latitude }!!.latitude
    val w = positions.minBy { it.longitude }!!.longitude
    val e = positions.maxBy { it.longitude }!!.longitude
    val h = (e - w) * 0.2
    val v = (n - s) * 0.2
    return BoundingBox(n + v, e + h, s - v, w - h)
  }
}

class Journey(
  private val mapView: MapView, val pictures: List<IPicture>, isRoute: Boolean = false
) : Overlay(), Disposable {

  var color: Int = Color.WHITE
    set(value) {
      route = makeRoute(value)
    }
  private var route: Polyline? = null
  internal val markers: List<CircleMarker>
  private val targets: List<AsyncTarget>
  val manager = DefaultOverlayManager(null)
  var onClick: ((IPicture) -> Boolean)? = null

  init {
    val resources = mapView.resources
    val metrics = resources.displayMetrics
    val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, mmThumbDiameter, metrics).toInt()
    markers = pictures.map {
      CircleMarker(px, resources).apply {
        onClick = { this@Journey.onClick?.invoke(it) ?: false }
        geo = it.meta.geo ?: GeoPoint(0.0, 0.0)
        isEnabled = false
      }
    }
    manager.addAll(markers)
    targets = pictures.map {
      GlideApp.with(mapView).load(it).circleCrop().into(AsyncTarget(px, px))
    }
    if (isRoute) {
      color = Color.WHITE
      manager.add(route)
    }
  }

  fun launchReceive(scope: CoroutineScope) {
    for ((marker, target) in markers.zip(targets)) {
      scope.launch(Dispatchers.Main) {
        marker.drawable = target.prepared.await()
        marker.isEnabled = true
        mapView.invalidate()
      }
    }
  }

  private fun makeRoute(color: Int): Polyline {
    val coords = pictures.mapNotNull { it.meta.geo }
    return Polyline().apply {
      setPoints(coords)
      this.color = color
    }
  }

  override fun draw(c: Canvas, osmv: MapView, shadow: Boolean) {
    manager.onDraw(c, osmv)
  }

  override fun dispose() {
    manager.clear()
    targets.forEach {
      it.request?.clear()
    }
  }
}

/**
 * 둥근 마커. 미리 동그라미로 깎은 Drawable을 설정해야 한다.
 */
class CircleMarker(pxSide: Int, val resources: Resources) : Overlay() {

  private val circle = ShapeDrawable(OvalShape()).apply {
    paint.color = Color.WHITE
    paint.style = Paint.Style.STROKE
  }
  var point = Point()
    private set

  var onClick: (() -> Boolean)? = null

  var drawable: Drawable? = null
    set(value) {
      field = (value ?: return).apply { bounds = circle.bounds }
    }

  var geo: GeoPoint = GeoPoint(0.0, 0.0)

  init {
    val bound = Rect(0, 0, pxSide, pxSide).apply { offset(-pxSide / 2, -pxSide / 2) }
    circle.bounds = bound
    circle.paint.strokeWidth = pxSide * 0.05f
  }

  fun setColor(color: Int) {
    circle.paint.color = color
  }

  override fun draw(c: Canvas, osmv: MapView, shadow: Boolean) {
    osmv.projection?.toPixels(geo, point)
    drawAt(c, drawable, point.x, point.y, false, osmv.mapOrientation)
    drawAt(c, circle, point.x, point.y, false, osmv.mapOrientation)
  }

  override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
    if (hitTest(e, mapView)) {
      onClick?.also { return it.invoke() }
    }
    return false
  }

  private fun hitTest(event: MotionEvent, mapView: MapView): Boolean {
    val d = drawable ?: return false

    mapView.projection.toPixels(geo, point)
    val screenRect = mapView.getIntrinsicScreenRect(null)
    val x = -point.x + screenRect.left + event.x.toInt()
    val y = -point.y + screenRect.top + event.y.toInt()
    return d.bounds.contains(x, y)
  }
}
