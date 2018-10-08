package kr.ac.kw.coms.globealbum.map

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import kr.ac.kw.coms.globealbum.common.GlideApp
import kr.ac.kw.coms.globealbum.common.getCircularBorderBitmap
import kr.ac.kw.coms.globealbum.provider.IPicture
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Polyline

class DiaryOverlays(
  val lmView: ILandmarkMapView,
  val pictures: List<IPicture>,
  isRoute: Boolean = false) :
  Marker.OnMarkerClickListener {

  internal val targets: List<GlideTarget>
  internal var route: Overlay? = null

  init {
    val requests = GlideApp.with(lmView.mapView)
    targets = pictures.map { requests.load(it).into(GlideTarget(it)) }
    if (isRoute) {
      route = getRoute()
      lmView.mapView.overlays.add(route)
    }
  }

  fun getRoute(): Polyline {
    val coords = pictures.map { it.meta.geo }.filterNotNull()
    val polyline = Polyline()
    polyline.setPoints(coords)
    polyline.color = 0xffffffff.toInt()
    lmView.mapView.overlays.add(polyline)
    return polyline
  }

  fun detach() {
    targets.forEach {
      it.request?.clear()
    }
    route?.also { lmView.mapView.overlays.remove(it) }
  }

  override fun onMarkerClick(marker: Marker?, mapView: MapView?): Boolean {
    if (marker is PictureMarker) {
      lmView.onTouchThumbnail(marker.picture)
    }
    return true
  }

  internal inner class GlideTarget(var picture: IPicture) : Target<Drawable> {

    val pxSide = 200
    var req: Request? = null
    var marker: PictureMarker? = null

    override fun onLoadStarted(placeholder: Drawable?) {}

    override fun getSize(cb: SizeReadyCallback) {
      cb.onSizeReady(pxSide, pxSide)
    }

    override fun removeCallback(_cb: SizeReadyCallback) {}

    override fun getRequest() = req

    override fun setRequest(request: Request?) {
      req = request
    }

    override fun onLoadFailed(errorDrawable: Drawable?) {}

    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
      val bm = BitmapDrawable(lmView.mapView.resources, getCircularBorderBitmap(resource, pxSide))
      marker = PictureMarker(picture, bm)
      lmView.mapView.overlays.add(marker)
    }

    override fun onLoadCleared(placeholder: Drawable?) {
      lmView.mapView.overlays.remove(marker);
    }

    override fun onStart() {}

    override fun onStop() {}

    override fun onDestroy() {}
  }

  internal inner class PictureMarker(val picture: IPicture, drawable: Drawable) : Marker(lmView.mapView) {
    init {
      icon = drawable
      picture.meta.geo?.also { position = it }
      setOnMarkerClickListener(this@DiaryOverlays)
    }
  }
}