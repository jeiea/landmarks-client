package kr.ac.kw.coms.globealbum.map

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import kr.ac.kw.coms.globealbum.common.GlideApp
import kr.ac.kw.coms.globealbum.common.GlideRequests
import kr.ac.kw.coms.globealbum.common.getCircularBitmap
import kr.ac.kw.coms.globealbum.provider.IPicture
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

class DiaryOverlays(val lmView: ILandmarkMapView, val pictures: List<IPicture>) :
  Marker.OnMarkerClickListener {

  internal val targets: List<GlideTarget>

  init {
    val requests: GlideRequests = GlideApp.with(lmView.mapView)
    targets = pictures.map { requests.load(it).into(GlideTarget(it)) }
  }

  fun getRoute(): Polyline {
    val coords = pictures.map { it.meta.geo }.filterNotNull()
    val polyline = Polyline()
    polyline.setPoints(coords)
    lmView.mapView.overlays.add(polyline)
    return polyline
  }

  internal inner class GlideTarget(var picture: IPicture) :
    CustomViewTarget<MapView, Drawable>(lmView.mapView) {

    var marker: PictureMarker? = null

    override fun onResourceCleared(placeholder: Drawable?) {
      view.overlays.remove(marker);
    }

    override fun onLoadFailed(errorDrawable: Drawable?) {}

    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
      val bm = BitmapDrawable(view.resources, getCircularBitmap(resource, 150))
      marker = PictureMarker(picture, bm)
      view.overlays.add(marker)
    }
  }

  fun detach() {
    targets.forEach {
      it.request?.clear()
    }
  }

  override fun onMarkerClick(marker: Marker?, mapView: MapView?): Boolean {
    if (marker is PictureMarker) {
      lmView.onTouchThumbnail(marker.picture)
    }
    return true
  }

  internal inner class PictureMarker(val picture: IPicture, icon: Drawable) : Marker(lmView.mapView) {
    init {
      image = icon
      picture.meta.geo?.also { position = it }
      setOnMarkerClickListener(this@DiaryOverlays)
    }
  }
}