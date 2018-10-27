package kr.ac.kw.coms.globealbum.map

import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.util.TypedValue
import android.view.MotionEvent
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import kr.ac.kw.coms.globealbum.common.GlideApp
import kr.ac.kw.coms.globealbum.provider.IPicture
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.DefaultOverlayManager
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Polyline
import kotlin.math.sqrt

private const val mmThumbDiameter = 10f

class DiaryOverlayFolder(private val mapView: MapView) : Overlay(), IDiaryOverlay {

  private val badgeOffset: Float
  private val px2: Float
  private val fontPaint: Paint

  private var journeyGroups = listOf<Journey>()
  private var journeyChains = listOf<Journey>()

  override var onThumbnailClick: ((IPicture) -> Boolean)? = null

  // Use cache
  override var groups
    get() = journeyGroups.map { g -> g.pictures }
    set(value) {
      val j = journeyGroups
      journeyGroups = value.map {
        Journey(mapView, it, false).apply { onClick = onThumbnailClick }
      }
      j.forEach(Journey::detach)
    }
  override var chains
    get() = journeyChains.map { g -> g.pictures }
    set(value) {
      val j = journeyChains
      journeyChains = value.map {
        Journey(mapView, it, true).apply { onClick = onThumbnailClick }
      }
      j.forEach(Journey::detach)
    }

  init {
    val metrics = mapView.resources.displayMetrics
    val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, mmThumbDiameter, metrics).toInt()
    badgeOffset = px * sqrt(2f) / 4
    px2 = (px * px).toFloat()
    fontPaint = Paint().apply {
      textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15f, metrics)
      color = (0xffff0000).toInt()
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
  }

  private fun forAllCircles(action: (Journey, CircleMarker) -> Boolean) {
    journeyGroups.forAllCircles(action)
    journeyChains.forAllCircles(action)
  }

  override fun addToSelection(picture: IPicture) {
    forAllCircles { _, marker ->
      if (marker.target.picture == picture) {
        marker.setColor(Color.YELLOW)
        true
      }
      else {
        false
      }
    }
  }

  override fun removeFromSelection(picture: IPicture) {
    forAllCircles { _, marker ->
      if (marker.target.picture == picture) {
        marker.setColor(Color.WHITE)
        true
      }
      else {
        false
      }
    }
  }

  override fun clearSelection() {
    forAllCircles { _, marker -> marker.setColor(Color.WHITE); false }
  }

  private fun List<Journey>.forAllCircles(action: (Journey, CircleMarker) -> Boolean) {
    forEach { group ->
      group.manager.forEach { overlay ->
        if (overlay is CircleMarker && action(group, overlay)) {
          return
        }
      }
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
    circles.forEach { m -> pj.toPixels(m.target.position, m.point) }
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
    return BoundingBox(n + 3, e + 3, s - 3, w - 3)
  }
}

internal class Journey(
  mapView: MapView, val pictures: List<IPicture>, isRoute: Boolean = false
) : Overlay() {

  var color: Int = Color.WHITE
    set(value) {
      route = makeRoute(value)
    }
  private var route: Polyline? = null
  private val targets: List<MarkerTarget>
  val manager = DefaultOverlayManager(null)
  var onClick: ((IPicture) -> Boolean)? = null

  init {
    val metrics = mapView.resources.displayMetrics
    val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, mmThumbDiameter, metrics).toInt()
    targets = pictures.mapNotNull { pic ->
      if (pic.meta.geo == null) null
      else {
        val target = MarkerTarget(px, pic) { circleMarker ->
          circleMarker.onClick = { onClick?.invoke(pic) ?: false }
          manager.add(circleMarker)
          mapView.invalidate()
        }
        GlideApp.with(mapView).load(pic).circleCrop().into(target)
      }
    }
    if (isRoute) {
      color = Color.WHITE
      manager.add(route)
    }
  }

  private fun makeRoute(color: Int): Polyline {
    val coords = targets.mapNotNull { it.picture.meta.geo }
    return Polyline().apply {
      setPoints(coords)
      this.color = color
    }
  }

  override fun draw(c: Canvas, osmv: MapView, shadow: Boolean) {
    manager.onDraw(c, osmv)
  }

  fun detach() {
    manager.clear()
    targets.forEach {
      it.request?.clear()
    }
  }
}

internal interface Disposable {
  fun dispose()
}

internal interface IMarkerData {
  val position: IGeoPoint
  val icon: Drawable
}

internal interface IMarkerTarget : Disposable, IMarkerData {
  val picture: IPicture
}

internal class MarkerTarget(
  private val px: Int, override val picture: IPicture, val onReady: (CircleMarker) -> Unit
) : Target<Drawable>, IMarkerTarget {

  private var req: Request? = null

  override fun dispose() {
    req?.clear()
  }

  override val position: IGeoPoint
    get() = picture.meta.geo!!

  override var icon: Drawable = ShapeDrawable()

  override fun onLoadStarted(placeholder: Drawable?) {
    placeholder?.also { icon = it }
  }

  override fun getSize(cb: SizeReadyCallback) {
    cb.onSizeReady(px, px)
  }

  override fun removeCallback(_cb: SizeReadyCallback) {}

  override fun getRequest() = req

  override fun setRequest(request: Request?) {
    req = request
  }

  override fun onLoadFailed(errorDrawable: Drawable?) {}

  override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
    icon = resource
    val m = CircleMarker(this)
    onReady(m)
  }

  override fun onLoadCleared(placeholder: Drawable?) {
  }

  override fun onStart() {}

  override fun onStop() {}

  override fun onDestroy() {}
}

internal class CircleMarker(val target: IMarkerTarget) : Overlay() {

  private val circle = ShapeDrawable(OvalShape()).apply {
    paint.color = Color.WHITE
    paint.style = Paint.Style.STROKE
  }
  var point = Point()
    private set
  var onClick: (() -> Boolean)? = null
  private var icon: Drawable? = null

  fun setColor(color: Int) {
    circle.paint.color = color
  }

  init {
    val px = target.icon.intrinsicHeight
    icon = target.icon
    val bound = Rect(0, 0, px, px).apply { offset(-px / 2, -px / 2) }
    target.icon.bounds = bound
    circle.bounds = bound
    circle.paint.strokeWidth = px * 0.05f
  }

  override fun draw(c: Canvas, osmv: MapView, shadow: Boolean) {
    osmv.projection?.toPixels(target.position, point)
    drawAt(c, icon, point.x, point.y, false, osmv.mapOrientation)
    drawAt(c, circle, point.x, point.y, false, osmv.mapOrientation)
  }

  override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
    if (hitTest(e, mapView)) {
      onClick?.also { return it.invoke() }
    }
    return false
  }

  override fun onDetach(mapView: MapView?) {
    super.onDetach(mapView)
    target.dispose()
  }

  private fun hitTest(event: MotionEvent, mapView: MapView): Boolean {
    mapView.projection.toPixels(target.position, point)
    val screenRect = mapView.getIntrinsicScreenRect(null)
    val x = -point.x + screenRect.left + event.x.toInt()
    val y = -point.y + screenRect.top + event.y.toInt()
    return target.icon.bounds.contains(x, y)
  }
}